# Common module

## Settings configuration file

All apps must have its own settings as **JSON** format.
A basic settings configuration file is of the form:

```
{
  "sync":
  {
    "url": "http://domain.my/webapi/",
    "token": 666,
    "status_url": "status/",
    "import_url": "import/",
    "exports":
    [
      {
        "url": "export/sqlite/",
        "file": "databases/data.db"
      },
      ...
    ]
  }
}
```

## Full Build
A full build can be executed with the following command:

```
../gradlew clean assembleDebug
```
