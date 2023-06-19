# Requests

OPENAI_API_KEY="key"
VERSION=gpt-3.5-turbo | gpt-4

The role of the messages author. One of system, user, assistant, or function.

curl https://api.openai.com/v1/chat/completions \
-X POST \
-H "Content-Type: application/json" \
-H "Authorization: Bearer $OPENAI_API_KEY" \
-d '{
"model": "gpt-3.5-turbo",
"messages": [{"role": "user", "content": "Как доехать от Софии до Будапешта?"}, {"role": "user", "content": "Hello!"}]
}'

List current models:
https://api.openai.com/v1/models

## Test requests

localhost:8080/api/ask-chat

## Links

Version of the API endpoints
https://platform.openai.com/docs/models/model-endpoint-compatibility

## Build

docker build . -t nefariusmag/chat_gpt:1.0.0