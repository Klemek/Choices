# API

## `/api` - General requests

Endpoint | Admin | Description
:--- | :---: | :---
[`GET /api`](#get-api) | ❌ | Get session information and lang hash
[`GET /api/lang`](#get-api-lang) | ❌ | Get all lang values
[`POST /api/lang`](#post-api-lang) | ✔ | Change lang values

## `/api/room` - Room related requests

Endpoint | Room master | Description
:--- | :---: | :---
[`PUT /api/room/create`](#put-api-room-create) | ❌ | Create a new room
[`GET /api/room/{id}`](#get-api-room-id-) | ✔ | Get a room info
[`POST /api/room/{id}`](#post-api-room-id-) | ✔ | Update a room
[`DELETE /api/room/{id}/kick/{userId}`](#delete-api-room-id-kick-userid-) | ✔ | Kick user from a room
[`DELETE /api/room/{id}/delete`](#delete-api-room-id-delete) | ✔ | Delete a room
[`POST /api/room/{id}/results`](#post-api-room-id-results) | ✔ | Delete a room
[`POST /api/room/{id}/join`](#post-api-room-id-join) | ❌ | Join a room
[`POST /api/room/{id}/answer/{ans}`](#post-api-room-id-answer-ans-) | ❌ | Answer to a room
[`DELETE /api/room/{id}/quit`](#delete-api-room-id-quit) | ❌ | Exit a room

## `/api/questions` - Question pack related requests

Endpoint | Admin | Description
:--- | :---: | :---
[`GET /api/questions/list`](#get-api-questions-list) | ❌ | List all question packs
[`GET /api/questions/all`](#get-api-questions-all) | ✔ | Get all question packs info
[`PUT /api/questions/create`](#put-api-questions-create) | ✔ | Get a question pack info
[`GET /api/questions/{id}`](#get-api-questions-id-) | ✔ | Get a question pack info
[`POST /api/questions/{id}`](#post-api-questions-id-) | ✔ | Update a question pack
[`DELETE /api/questions/{id}`](#delete-api-questions-id-) | ✔ | Delete a question pack

# Format

<details><summary><b>Valid Response (JSON)</b></summary><p>

```JSON
{
    "code" : 200
    "value" : "object"
}
```

</p></details><details><summary><b>Error Response (JSON)</b></summary><p>

```JSON
{
    "code" : "int"
    "error" : "string"
}
```

</p></details>

# `/api`
## `GET /api`
Get session information and other stuff (optional values are not there if no session)
<details><summary><b>Response (JSON)</b></summary><p>

```JSON
"value" : {
    "langHash" : "long"
    "appPath" : "string"
    "userEmail" : "string (optional)",
    "userName" : "string (optional)"
    "userId" : "string (optional)",
    "userImageUrl" : "string (optional)",
    "admin": "boolean (optional)",
}
```

</p></details>

## `GET /api/lang`
Get all lang values
<details><summary><b>Response (JSON)</b></summary><p>

```JSON
"value" : {
    "hash" : "long",
    "lang" : {
        "key" : "string",
        ...
    }
}
```

</p></details>

## `POST /api/lang`
Change lang values (need to be admin)
<details><summary><b>Request (JSON)</b></summary><p>

```JSON
{
    "lang" : {
        "key" : "string",
        ...
    }
}
```

</p></details><details><summary><b>Response (JSON)</b></summary><p>

```JSON
"value" : {
    "hash" : "int",
    "lang" : {
        "key" : "string",
        ...
    }
}
```

</p></details>

# `/api/room`
## `PUT /api/room/create`
Create a new room
<details><summary><b>Request (JSON)</b></summary><p>

```JSON
{
	"packId" : "int",
	"lock" : "boolean (optional)"
}
```

</p></details><details><summary><b>Response (JSON)</b></summary><p>

```JSON
"value" : {
	"id" : "string",
	"lock" : "boolean",
	"lockAnswers" : "boolean",
	"users" : [{
		"id" : "string",
		"name" : "string",
		"imageUrl" : "string",
		"answer" : "int"
	}],
	"pack" : {
		"id" : "int",
		"name" : "string",
		"video" : "string",
		"message" : "string",
		"questions" : [{
			"text" : "string",
			"answers" : ["string"],
			"links" : ["string"]
		}]
	}
}
```

</p></details>

## `GET /api/room/{id}`
Get a room info (need to be room master)
<details><summary><b>Response (JSON)</b></summary><p>

```JSON
"value" : {
	"id" : "string",
	"lock" : "boolean",
	"lockAnswers" : "boolean",
	"users" : [{
		"id" : "string",
		"name" : "string",
		"imageUrl" : "string",
		"answer" : "int"
	}]
}
```

</p></details>

## `POST /api/room/{id}`
Update a room (need to be room master)
<details><summary><b>Request (JSON)</b></summary><p>

```JSON
{
	"lock" : "boolean (optional)",
	"lockAnswers" : "boolean (optional)",
	"reset" : "any (optional)"
}
```

</p></details><details><summary><b>Response (JSON)</b></summary><p>

```JSON
"value" : {
	"id" : "string",
	"lock" : "boolean",
	"lockAnswers" : "boolean",
	"users" : [{
		"id" : "string",
		"name" : "string",
		"imageUrl" : "string",
		"answer" : "int"
	}]
}
```

</p></details>

## `DELETE /api/room/{id}/kick/{userId}`
Kick user from a room (need to be room master)
## `DELETE /api/room/{id}/delete`
Delete a room (need to be room master)
## `POST /api/room/{id}/results`
Send results of a room to the system admin (need to be room master)
<details><summary><b>Request (JSON)</b></summary><p>

```JSON
{
    "datetime" : "int"
	"duration" : "int",
	"target" : "int",
	"packId": "int",
	"users" : [{
	    "id" : "string",
	    "name" : "string",
	    "email" : "string (optional)",
	    "score" : "int",
	    "groupTeaching" : "int",
	    "teachingFailed" : "int"
	}],
	"questions" : [{
	    "right" : "int",
	    "wrong" : "int",
	    "unanswered" : "int",
	    "score" : "int"
	}],
	"videos" : [string],
	"teachers" : [[string]]
}
```

</p></details>

## `POST /api/room/{id}/join`
Join a room
## `POST /api/room/{id}/answer/{ans}`
Answer to a room (answer integer between 0 and 4)
## `DELETE /api/room/{id}/quit`
Exit a room

# `/api/questions`
## `GET /api/questions/list`
List all enabled question packs
<details><summary><b>Response (JSON)</b></summary><p>

```JSON
"value" : [{
    "id" : "int",
    "name" : "string",
    "questionCount" : "int"
}]
```

</p></details>

## `GET /api/questions/all`
Get all question packs info (need to be admin)
<details><summary><b>Response (JSON)</b></summary><p>

```JSON
"value" : [{
    "id" : "int",
    "name" : "string",
    "message" : "string",
    "enabled" : "boolean",
    "questions" : [{
        "text" : "string",
        "answers" : ["string"],
        "links" : ["string"]
    }]
}]
```

</p></details>

## `PUT /api/questions/create`
Create a new question pack (need to be admin)
<details><summary><b>Request (JSON)</b></summary><p>

```JSON
{
    "name" : "string",
    "video" : "string (optional)",
    "message" : "string (optional)",
    "enabled" : "boolean (optional)",
    "questions" : [{
        "text" : "string",
        "answers" : ["string"],
        "links" : ["string"]
    }]
}
```

</p></details><details><summary><b>Response (JSON)</b></summary><p>

```JSON
"value" : {
    "id" : "int",
    "name" : "string",
    "video" : "string",
    "message" : "string",
    "enabled" : "boolean",
    "questions" : [{
        "text" : "string",
        "answers" : ["string"],
        "links" : ["string"]
    }]
}
```

</p></details>

## `GET /api/questions/{id}`
Get a question pack info (need to be admin)
<details><summary><b>Response (JSON)</b></summary><p>

```JSON
"value" : {
    "id" : "int",
    "name" : "string",
    "video" : "string",
    "message" : "string",
    "enabled" : "boolean",
    "questions" : [{
        "text" : "string",
        "answers" : ["string"],
        "links" : ["string"]
    }]
}
```

</p></details>

## `POST /api/questions/{id}`
Update a question pack (need to be admin)
<details><summary><b>Request (JSON)</b></summary><p>

```JSON
{
    "name" : "string",
    "video" : "string (optional)",
    "message" : "string (optional)",
    "enabled" : "boolean (optional)",
    "questions" : [{
        "text" : "string",
        "answers" : ["string"],
        "links" : ["string"]
    }]
}
```

</p></details><details><summary><b>Response (JSON)</b></summary><p>

```JSON
"value" : {
    "id" : "int",
    "name" : "string",
    "video" : "string",
    "message" : "string",
    "enabled" : "boolean",
    "questions" : [{
        "text" : "string",
        "answers" : ["string"],
        "links" : ["string"]
    }]
}
```

</p></details>

## `DELETE /api/questions/{id}`
Delete a question pack (need to be admin)