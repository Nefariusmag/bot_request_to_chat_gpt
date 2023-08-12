# Bot to make requests to OpenAI API

## How to make requests

OPENAI_API_KEY="key"
VERSION=gpt-3.5-turbo | gpt-4

The role of the messages author. One of system, user, assistant, or function.

```shell
curl https://api.openai.com/v1/chat/completions \
-X POST \
-H "Content-Type: application/json" \
-H "Authorization: Bearer $OPENAI_API_KEY" \
-d '{"model": "gpt-4","messages": [{"role": "user", "content": "Как доехать от Софии до Будапешта?"}]}'
```

```shell
curl https://api.openai.com/v1/models \
-H "Content-Type: application/json" \
-H "Authorization: Bearer $OPENAI_API_KEY" 
```

```shell
curl https://api.openai.com/v1/models -H "Authorization: Bearer $OPENAI_API_KEY"
```

## Test requests

```shell
docker run -p 5432:5432 -e POSTGRES_PASSWORD=1q2w3e -it --rm postgres:13-alpine
```

localhost:8080/api/ask-chat

## Links

Version of the API endpoints
https://platform.openai.com/docs/models/model-endpoint-compatibility

List current models:
https://api.openai.com/v1/models

## Build and release

```shell
docker build . -t nefariusmag/chat_gpt:1.0.3 --platform=linux/amd64 
docker push nefariusmag/chat_gpt:1.0.3
```

```shell
sudo docker pull nefariusmag/chat_gpt:1.0.3
sudo docker-compose up -d
```

## TODO
+ Use spring context to inject OpenAI API key and classes
+ Start to write questions and answers to DB 
+ Create a context for each user, every question and answer should be in context
+ Understand why chatGPT return 400 error when I send request with context
+ Make limits for context, 4096 symbols
+ Create new `/new` endpoint to create new context
+ Fix bug with special symbols
+ Write new class to make requests to OpenAI API
+ Add select version of the API
+ Add transaction to save question only if get answer
- Understand how to use graalvm to build native image
- Understand how to update bot, because 'TelegramLongPollingBot() is deprecated'

