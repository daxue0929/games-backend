# games-backend
> games backend project for yy

## 前端地址
https://github.com/CloudBillow/flappy-word

### flappy-word相关接口文档
**登录**

`POST  application/json`

| 参数       | 类型     | 是否为空 | 解释    |
|----------|--------|------|-------|
| username | string | 否    | 登录用户名 |
| code     | string | 否    | 登录凭证  |

```bash
# 登录接口返回token,refreshToken凭证
# token         有效期 1h
# refreshToken  有效期 8h
curl --location 'http://localhost:9001/login' \
--header 'Content-Type: application/json' \
--data '{
    "username": "daxue",
    "code": ""
}'
```
**刷新token**

`POST  application/json`

| 参数           | 类型     | 是否为空 | 解释        |
|--------------|--------|------|-----------|
| refreshToken | string | 否    | 用于刷新token |
```bash
curl --location 'http://localhost:9001/login/refreshToken' \
--header 'Content-Type: application/json' \
--data '{
    "refreshToken": "your refreshToken凭证"
}'
```

**上报成绩**

`POST  application/json`

| 参数     | 类型  | 是否为空 | 解释  |
|--------|-----|------|-----|
| score  | int | 否    | 分数  |
| hurdle | int | 否    | 关卡  |

```bash
curl --location 'http://localhost:9001/ranking' \
--header 'Authorization: Bearer token' \
--header 'Content-Type: application/json' \
--data '{
    "score": 10,
    "hurdle": 2
}'
```
**排行榜**

`GET  application/json`

```bash
curl --location 'http://localhost:9001/ranking' \
--header 'Authorization: Bearer token'
```

**当前用户排名**

`GET  application/json`

```bash
curl --location 'http://localhost:9001/ranking/current' \
--header 'Authorization: Bearer token'
```


#### 状态码定义
```
org.daxue.games.entity.common.ResultCode
```