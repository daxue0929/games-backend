# games-backend
> games backend project for yy

### 前端地址
https://github.com/CloudBillow/flappy-word

#### flappy-word接口
**登录**
```bash
curl --location 'http://localhost:9001/login' \
--header 'Content-Type: application/json' \
--data '{
    "username": "daxue",
    "code": "123456"
}'
```
**上报**
```bash
curl --location 'http://localhost:9001/ranking' \
--header 'Content-Type: application/json' \
--data '{
    "userId": "user-d571cacb107f4cdba802f08572345fda",
    "score": 8,
    "hurdle": 2
}'
```
**排行榜**
```bash
curl --location 'http://localhost:9001/ranking'
```