package org.daxue.games.validation;

import lombok.Data;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.daxue.games.entity.req.UserAction;

import java.util.*;

@Data
@Builder
@Slf4j
public class GameScoreValidation {
    private static final double MIN_INTERVAL_TIME = 300; // 最小间隔时间
    private static final double MAX_INTERVAL_TIME = 1500; // 最大间隔时间
    private static final double INITIAL_PIPE_SPEED = 1.5;
    private static final double SPEED_INCREASE = 0.01;
    private static final double PIPE_SPACING = 300;
    private static final int FRAME_INTERVAL = 16;
    private static final int SCORE_TO_INCREASE_SPEED = 6;
    private static final int THROUGH_SCORE = 3;
    private static final int JUMP_SCORE = 1;
    private static final double MIN_THROUGH_INTERVAL = 2000; // 最小管道通过间隔(毫秒)

    private List<UserAction> actions;
    private int reportedScore;
    private int reportedThroughCount;
    private List<String> validationErrors;

    public boolean validate() {
        validationErrors = new ArrayList<>();

        if (actions == null) {
            validationErrors.add("游戏数据为空，无法验证");
            return false;
        }

        // 跳跃间隔时间
        validateReactionTimes();
        // 通过间隔时间
        validateThroughIntervals();
        // 分数
        validateScore();
        // 字母频率
        validateActionSequence();

        return validationErrors.isEmpty();
    }

    private void validateReactionTimes() {
        // 获取所有jump行为
        List<UserAction> jumpActions = actions.stream()
                .filter(action -> "jump".equals(action.getAction()))
                .toList();

        // 如果jump行为少于2个，直接返回
        if (jumpActions.size() < 2) return;

        // 检查相邻jump之间的时间间隔
        for (int i = 1; i < jumpActions.size(); i++) {
            UserAction currentJump = jumpActions.get(i);
            UserAction previousJump = jumpActions.get(i - 1);

            long timeDiff = currentJump.getTime() - previousJump.getTime();

            if (timeDiff < MIN_INTERVAL_TIME) {
                validationErrors.add(String.format(
                        "检测到可疑的快速跳跃：在第'%d'次(字母'%s')和第'%d'次(字母'%s')之间的间隔为 %d 毫秒，小于正常反应时间",
                        i,
                        previousJump.getLetter(),
                        i + 1,
                        currentJump.getLetter(),
                        timeDiff
                ));
            }

            if (timeDiff > MAX_INTERVAL_TIME) {
                validationErrors.add(String.format(
                        "检测到可疑的慢速跳跃：在第'%d'次(字母'%s') 和 第'%d'次(字母'%s')之间的间隔为 %d 毫秒，间隔时间过长",
                        i,
                        previousJump.getLetter(),
                        i + 1,
                        currentJump.getLetter(),
                        timeDiff
                ));
            }
        }
    }

    private void validateThroughIntervals() {
        List<UserAction> throughActions = actions.stream()
                .filter(a -> "through".equals(a.getAction()))
                .toList();

        if (throughActions.size() < 2) return;

        for (int i = 1; i < throughActions.size(); i++) {
            UserAction current = throughActions.get(i);
            UserAction previous = throughActions.get(i - 1);

            // 计算前一个through时的分数
            int previousScore = calculateScoreAtTime(previous.getTime());
            // 计算当前管道速度
            double currentPipeSpeed = calculatePipeSpeed(previousScore);

            // 计算最小通过间隔（毫秒）
            // 管道间距300px，每16ms移动currentPipeSpeed个像素
            // 所以移动300px需要的时间 = (300 / currentPipeSpeed) * 16
            double minExpectedInterval = (PIPE_SPACING / currentPipeSpeed) * 16;
            // 允许30%的误差范围（因为实际游戏中可能会有一些加速或减速）
            double minAllowedInterval = minExpectedInterval * 0.7;

            double actualInterval = current.getTime() - previous.getTime();

            if (actualInterval < minAllowedInterval) {
                validationErrors.add(String.format(
                        "检测到异常的管道通过间隔：在第 %d 个通过处的间隔为 %.2f 毫秒，" +
                                "当前速度 %.2f px/frame，最小允许间隔 %.2f 毫秒（理论间隔 %.2f 毫秒）",
                        i, actualInterval, currentPipeSpeed, minAllowedInterval, minExpectedInterval
                ));
            }
        }
    }

    private double calculatePipeSpeed(int score) {
        return INITIAL_PIPE_SPEED + SPEED_INCREASE * Math.floor((double) score / SCORE_TO_INCREASE_SPEED);
    }

    private int calculateScoreAtTime(long timestamp) {
        return actions.stream()
                .filter(a -> a.getTime() <= timestamp)
                .mapToInt(a -> "through".equals(a.getAction()) ? THROUGH_SCORE :
                        "jump".equals(a.getAction()) ? JUMP_SCORE : 0)
                .sum();
    }

    private void validateScore() {
        // 计算through的次数
        long throughCount = actions.stream()
                .filter(a -> "through".equals(a.getAction()))
                .count();

        // 计算有效jump的次数
        long jumpCount = actions.stream()
                .filter(a -> "jump".equals(a.getAction()))
                .count();

        int expectedScore = (int) (throughCount * THROUGH_SCORE + jumpCount * JUMP_SCORE);

        if (reportedScore != expectedScore) {
            validationErrors.add(String.format(
                    "分数计算不匹配：上报分数为 %d，预期分数应为 %d",
                    reportedScore, expectedScore
            ));
        }

        if (reportedThroughCount != throughCount) {
            validationErrors.add(String.format(
                    "穿过管道次数不匹配：上报次数为 %d，实际计算次数为 %d",
                    reportedThroughCount, throughCount
            ));
        }
    }

    private void validateActionSequence() {

        // 每个字母跳跃时间记录
        Map<String, List<Long>> letterTimestamps = new HashMap<>();

        for (UserAction action : actions) {
            if ("jump".equals(action.getAction()) && action.getLetter() != null) {
                String letter = action.getLetter();
                long currentTime = action.getTime();

                // 获取或创建该字母的时间戳列表
                letterTimestamps.computeIfAbsent(letter, k -> new ArrayList<>())
                        .add(currentTime);
            }
        }

        for (Map.Entry<String, List<Long>> entry : letterTimestamps.entrySet()) {
            String letter = entry.getKey();
            List<Long> timestamps = entry.getValue();

            if (timestamps.size() < 3) {
                continue; // 出现次数太少，忽略
            }

            int suspiciousPatterns = 0;
            long timeWindow = 3000; // 3秒时间窗口
            int minRepeatThreshold = 3; // 时间窗口内最少重复次数

            for (int i = 0; i < timestamps.size(); i++) {
                int repeatCount = 1;
                long windowStart = timestamps.get(i);

                for (int j = i + 1; j < timestamps.size(); j++) {
                    if (timestamps.get(j) - windowStart <= timeWindow) {
                        repeatCount++;
                    } else {
                        break;
                    }
                }

                if (repeatCount >= minRepeatThreshold) {
                    suspiciousPatterns++;
                }
            }

            // 校验生成字母的重复度
            if (suspiciousPatterns > 0) {
                double suspiciousRatio = (double) suspiciousPatterns / timestamps.size();
                System.out.println("suspiciousRatio = " + suspiciousRatio);
                if (suspiciousRatio > 0.3) {
                    validationErrors.add(String.format(
                            "字母'%s'存在异常：在%d秒内重复出现%d次",
                            letter,
                            timeWindow / 1000,
                            suspiciousPatterns
                    ));
                }
            }
        }
    }

    public static void main(String[] args) throws ParseException {

        String jsonString = "[{\"action\":\"jump\",\"letter\":\"C\",\"time\":1732244306564},{\"action\":\"jump\",\"letter\":\"C\",\"time\":1732244507711},{\"action\":\"jump\",\"letter\":\"C\",\"time\":173224508662},{\"action\":\"through\",\"time\":1732244509347},{\"action\":\"jump\",\"letter\":\"C\",\"time\":1732244509491},{\"action\":\"jump\",\"letter\":\"T\",\"time\":1732244510208},{\"action\":\"jump\",\"letter\":\"S\",\"time\":1732244511119},{\"action\":\"jump\",\"letter\":\"D\",\"time\":173224452020},{\"action\":\"jump\",\"letter\":\"E\",\"time\":1732244512970},{\"action\":\"through\",\"time\":1732244513061},{\"action\":\"jump\",\"letter\":\"R\",\"time\":1732244513817},{\"action\":\"jump\",\"letter\":\"G\",\"time\":1732244514649},{\"action\":\"jump\",\"letter\":\"J\",\"time\":1732244515577},{\"action\":\"jump\",\"letter\":\"B\",\"time\":1732244516500},{\"action\":\"through\",\"time\":1732244516723},{\"action\":\"jump\",\"letter\":\"A\",\"time\":1732244517404},{\"action\":\"jump\",\"letter\":\"Q\",\"time\":1732244518295},{\"action\":\"jump\",\"letter\":\"D\",\"time\":1732244519180},{\"action\":\"jump\",\"letter\":\"K\",\"time\":1732244520051},{\"action\":\"through\",\"time\":1732244520383},{\"action\":\"jump\",\"letter\":\"N\",\"time\":1732244520903},{\"action\":\"jump\",\"letter\":\"P\",\"time\":1732244521745},{\"action\":\"jump\",\"letter\":\"N\",\"time\":1732244522661},{\"action\":\"jump\",\"letter\":\"W\",\"time\":1732244523263},{\"action\":\"through\",\"time\":1732244523955},{\"action\":\"jump\",\"letter\":\"Q\",\"time\":1732244524209},{\"action\":\"jump\",\"letter\":\"Q\",\"time\":1732244524923},{\"action\":\"jump\",\"letter\":\"R\",\"time\":1732244525595},{\"action\":\"jump\",\"letter\":\"Q\",\"time\":1732244526622},{\"action\":\"jump\",\"letter\":\"F\",\"time\":1732244527229},{\"action\":\"through\",\"time\":1732244527504},{\"action\":\"jump\",\"letter\":\"X\",\"time\":1732244528227},{\"action\":\"jump\",\"letter\":\"Q\",\"time\":1732244529499},{\"action\":\"jump\",\"letter\":\"C\",\"time\":1732244530339},{\"action\":\"through\",\"time\":1732244531065},{\"action\":\"jump\",\"letter\":\"V\",\"time\":1732244531189},{\"action\":\"jump\",\"letter\":\"P\",\"time\":1732244532176},{\"action\":\"jump\",\"letter\":\"I\",\"time\":1732244532829},{\"action\":\"jump\",\"letter\":\"I\",\"time\":1732244533394},{\"action\":\"jump\",\"letter\":\"O\",\"time\":1732244534083},{\"action\":\"through\",\"time\":1732244534592},{\"action\":\"jump\",\"letter\":\"D\",\"time\":1732244534975},{\"action\":\"jump\",\"letter\":\"E\",\"time\":1732244535662},{\"action\":\"jump\",\"letter\":\"G\",\"time\":1732244536258}]";

        JSONParser parser = new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE);
        JSONArray jsonArray = (JSONArray) parser.parse(jsonString);

        List<UserAction> userActions = new ArrayList<>();

        for (Object obj : jsonArray) {
            JSONObject jsonObj = (JSONObject) obj;
            UserAction action = new UserAction();

            action.setAction((String) jsonObj.get("action"));
            action.setLetter((String) jsonObj.get("letter"));
            action.setTime((Long) jsonObj.get("time"));

            userActions.add(action);
        }

        // 验证器
        GameScoreValidation validator = GameScoreValidation.builder()
                .actions(userActions)
                .reportedScore(60)
                .reportedThroughCount(8)
                .build();

        boolean isValid = validator.validate();

        if (!isValid) {
            List<String> errors = validator.getValidationErrors();
            // 输出错误信息
            for (String error : errors) {
                System.out.println(error);
            }
        }
    }
}