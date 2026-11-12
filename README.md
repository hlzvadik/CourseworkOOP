# Агрегатор данных из REST API (Этап 1)

Консольное Java-приложение, которое опрашивает три открытых REST API, приводит полученные данные к единой модели и сохраняет их в файл (JSON или CSV). Приложение умеет читать файл и выводить его на экран целиком или по конкретному источнику.

## Используемые технологии
* **Java 26** (`java.time`, `java.net.http.HttpClient`, record-классы)
* **Maven** (система сборки, `maven-shade-plugin` для запускаемого jar)
* **Jackson** (`jackson-databind`, `jackson-datatype-jsr310`) для разбора и записи JSON
* **Apache Commons CSV** для чтения и записи CSV
* **SLF4J + Logback** (логирование в консоль и в файл `logs/app.log`)
* **Lombok** (`@Slf4j`, геттеры и сеттеры)
* **Java NIO** (работа с файловой системой: `Files`, `Path`)

## Выбранные API

Каждое API принимает query-параметры, возвращает JSON с более чем 5 полями, и данные в нём меняются со временем. Параметры, указанные ниже, это значения по умолчанию, они заданы в классах `entities/request/*Api.java`. Ключи не нужны.

1. **Open-Meteo (Погода)**, имя источника `weather`: `https://api.open-meteo.com/v1/forecast`
    * Возвращает текущую погоду. В ответе вложенные объекты `current` и `current_units`.
    * Параметры: `latitude` (`60.00`), `longitude` (`30.38`), `current` (`temperature_2m,wind_speed_10m,relative_humidity_2m`).
    * Сохраняем (одна запись на запрос): `current.time`, `temperature_2m`, `wind_speed_10m`, `relative_humidity_2m`.
2. **Hacker News Search, Algolia (Новости)**, имя источника `hackernews`: `https://hn.algolia.com/api/v1/search`
    * Возвращает найденные посты. В ответе массив `hits[]`, а внутри него массив `_tags[]` и объект `_highlightResult`.
    * Параметры: `query` (`java`), `tags` (`story`), `hitsPerPage` (`5`).
    * Сохраняем (одна запись на каждый элемент `hits[]`): `created_at`, `title`, `author`, `points`, `num_comments`.
3. **CoinGecko (Криптовалюты)**, имя источника `crypto`: `https://api.coingecko.com/api/v3/coins/markets`
    * Возвращает массив объектов, по одному на монету, с ценой, капитализацией, объёмом торгов и другими показателями.
    * Параметры: `vs_currency` (`usd`), `ids` (`bitcoin,ethereum`).
    * Сохраняем (одна запись на монету): `last_updated`, `name`, `current_price`, `market_cap`, `price_change_percentage_24h`.
    * У бесплатного доступа есть лимит запросов. При ответе 401 или 429 источник завершается ошибкой, остальные источники опрашиваются дальше.

Все остальные поля ответов при разборе игнорируются.

## Единая модель

Ответы всех API превращаются в один вид записи `AggregatedRecord`:

| Поле | Смысл |
|---|---|
| `source` | имя источника: `weather`, `hackernews`, `crypto` |
| `params` | параметры, с которыми сделан запрос (`key=value&key=value`) |
| `timestamp` | время данных (`Instant`, UTC) |
| `values` | показатели, `Map<String, String>` |

Ключи в `values`: у `weather` это `temperature`, `windSpeed`, `humidity`; у `hackernews` это `title`, `author`, `points`, `numComments`; у `crypto` это `coin`, `price`, `marketCap`, `change24h`. Записи всех источников лежат в одном файле и различаются полем `source`.

## Архитектура

Проект разделён на слои для соблюдения принципов ООП и разделения ответственности:
* **Консоль** (`Main`, `SettingsParser`, `ConsolePrinter`): разбор аргументов командной строки, вывод на экран, коды завершения.
* **Сборка** (`AppContext`): создаёт объекты, хранит их и передаёт зависимости через конструкторы.
* **Ядро** (`Aggregator`): опрашивает выбранные источники, собирает записи, передаёт их в хранилище, читает файл и фильтрует по `source`.
* **Источники** (`DataSource<T>` и реализации `OpenMeteoSource`, `HackerNewsSource`, `CoinGeckoSource`): запрос, разбор JSON в Response-record и маппинг в единую модель. Параметры запроса по умолчанию лежат в `*Api` (наследники `ApiForRequest`), форма ответа описана record-классами `*Response`.
* **HTTP** (`HttpFetcher`, `HttpApiClient`): выполнение запросов, таймауты, проверка статуса, перевод ошибок в `ApiException`.
* **Хранилище** (интерфейс `Storage` и реализации `JsonStorage`, `CsvStorage`).
* **Единая модель** (`AggregatedRecord`).

*Новый API добавляется созданием классов `*Api`, `*Response` и `*Source` (реализация `DataSource`) и одной строки `register(...)` в `AppFactory`, без изменения существующей логики.*

## Сборка и запуск

### Сборка проекта
```bash
mvn clean package
```
Результат: `target/app.jar` со всеми зависимостями.

### Параметры командной строки

| Параметр | Обязателен | Значения | Смысл |
|---|---|---|---|
| `--sources` | да, при опросе | `weather`, `hackernews`, `crypto` через запятую | какие API опрашивать |
| `--fileType` | да | `json` или `csv` | формат файла |
| `--writeType` | да, при опросе | `new` или `append` | создать новый файл или дозаписать в существующий |
| `--outputFile` | нет | путь | файл для записи и чтения. По умолчанию `data.json` или `data.csv` по формату |
| `--show` | нет | список источников через запятую (необязателен) | вывести содержимое файла. Без списка выводится всё |

`--show` нельзя сочетать с `--sources` и `--writeType`. Для `--show` нужно указывать тот же `--fileType` и `--outputFile`, что и при записи.

### Запуск проекта
#### 1. Опрос API и сохранение в JSON (создать новый файл)
```bash
java -jar target/app.jar --sources weather,hackernews,crypto --fileType json --writeType new
```
#### 2. Опрос API и сохранение в CSV
```bash
java -jar target/app.jar --sources weather,crypto --fileType csv --writeType new
```
#### 3. Дозапись в существующий файл (режим APPEND)
```bash
java -jar target/app.jar --sources weather --fileType json --writeType append
```
#### 4. Свой путь к файлу
```bash
java -jar target/app.jar --sources weather --fileType json --writeType new --outputFile results/weather.json
```
#### 5. Просмотр всего файла
```bash
java -jar target/app.jar --show --fileType json
```
#### 6. Просмотр только определённых источников
```bash
java -jar target/app.jar --show weather,crypto --fileType json
```

## Формат файлов

JSON (массив записей):
```json
[ {
  "source" : "weather",
  "params" : "latitude=60.00&longitude=30.38&current=temperature_2m,wind_speed_10m,relative_humidity_2m",
  "timestamp" : "2026-11-12T10:00:00Z",
  "values" : { "temperature" : "5.3", "windSpeed" : "12.1", "humidity" : "80" }
} ]
```

CSV: одна шапка `source,params,timestamp,values`. Колонка `values` хранится JSON-строкой, чтобы запятые и кавычки в данных (например, в заголовках постов) не ломали файл.

В режиме `append` для JSON старый массив читается, к нему добавляются записи, и файл записывается заново. Для CSV строки дописываются в конец, шапка пишется только в новый или пустой файл.

## Логирование

Каждый опрос источника логируется двумя строками:

```
10:00:03.120 INFO  Aggregator - [START] weather
10:00:03.415 INFO  Aggregator - [END] weather OK records=1
```

Кроме этого логируются HTTP-запросы (адрес, статус, время в миллисекундах) и операции с файлами.

## Обработка ошибок
При неверных аргументах программа выводит понятную ошибку валидации и завершается с кодом 1. Если внешнее API недоступно, агрегатор логирует ошибку и продолжает опрос остальных источников. Если не удалось получить данные ни от одного источника, файл не изменяется, а код завершения равен 1.