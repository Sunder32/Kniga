# Kniga - Офлайн Читалка для Android

## Описание
Мобильное приложение для Android для чтения электронных книг в офлайн-режиме с автоматической синхронизацией между устройствами.

## Ключевые функции
- ✅ Чтение книг без интернета (EPUB, PDF, FB2, MOBI)
- ✅ Автоматическая синхронизация прогресса между устройствами
- ✅ Закладки и выделения текста с заметками
- ✅ Статистика чтения (время, страницы, серии чтения)
- ✅ Гибкие настройки отображения (темы, шрифты, размеры)
- ✅ Offline-first архитектура

## Технологии

### Android
- **Язык**: Kotlin
- **Минимальная версия Android**: 7.0 (API 24)
- **Target SDK**: 36
- **Архитектура**: MVVM + Clean Architecture

### Основные библиотеки
- **Room Database** - локальное хранилище данных
- **Retrofit 2** - сетевые запросы
- **Coroutines** - асинхронность
- **Flow** - реактивные потоки данных
- **WorkManager** - фоновая синхронизация
- **Navigation Component** - навигация
- **Jetpack Compose** - современный UI
- **DataStore** - хранение настроек
- **Coil** - загрузка изображений

## Структура проекта

```
app/src/main/java/com/example/kniga/
├── data/
│   ├── local/
│   │   ├── entity/          # Room entities (Book, Bookmark, etc.)
│   │   ├── dao/             # DAO интерфейсы
│   │   └── AppDatabase.kt   # Главная база данных
│   ├── remote/
│   │   ├── api/             # Retrofit API интерфейсы
│   │   └── dto/             # Data Transfer Objects
│   └── repository/          # Repository слой
│       ├── BookRepository.kt
│       ├── SyncRepository.kt
│       └── UserRepository.kt
├── domain/
│   ├── model/               # Доменные модели
│   └── usecase/             # Use cases
├── presentation/
│   ├── ui/
│   │   ├── splash/          # Экран приветствия
│   │   ├── auth/            # Вход/Регистрация
│   │   ├── library/         # Библиотека книг
│   │   ├── reader/          # Читалка
│   │   ├── bookmarks/       # Закладки
│   │   ├── statistics/      # Статистика
│   │   └── settings/        # Настройки
│   └── viewmodel/           # ViewModels
├── utils/
│   ├── epub/                # EPUB парсер
│   ├── pdf/                 # PDF парсер
│   ├── sync/                # Синхронизация
│   └── Constants.kt
└── MainActivity.kt
```

## Модели данных (Room Entities)

### Book
- Основная информация о книге
- Путь к файлу, формат, обложка
- Прогресс чтения (0-100%)
- Статусы: NOT_STARTED, READING, COMPLETED

### ReadingProgress
- История прогресса чтения
- Текущая страница и позиция
- Время чтения

### Bookmark
- Закладки в книгах
- Превью текста
- Заметки пользователя

### Highlight
- Выделения текста
- Цвета (желтый, зеленый, синий, розовый, оранжевый)
- Заметки к выделениям

### ReadingSession
- Сессии чтения для статистики
- Длительность, прочитанные страницы
- Скорость чтения (слов/минуту)

### User
- Информация о пользователе
- Токены авторизации
- Настройки синхронизации

### SyncQueueItem
- Очередь синхронизации
- Хранит изменения для отправки на сервер

## Установка и запуск

### Требования
- Android Studio Hedgehog или новее
- JDK 11 или новее
- Android SDK 24+

### Шаги установки
1. Клонируйте репозиторий:
```bash
git clone [repository-url]
cd Kniga
```

2. Откройте проект в Android Studio

3. Синхронизируйте Gradle:
```bash
./gradlew build
```

4. Запустите приложение на эмуляторе или устройстве

## Разработка

### Gradle Sync
После изменения dependencies выполните:
```bash
./gradlew --refresh-dependencies
```

### Сборка
```bash
# Debug
./gradlew assembleDebug

# Release
./gradlew assembleRelease
```

### Тесты
```bash
# Unit тесты
./gradlew test

# Инструментальные тесты
./gradlew connectedAndroidTest
```

## Архитектура

### Offline-First подход
Приложение всегда работает с локальной базой данных. Синхронизация происходит в фоне:
1. Пользователь выполняет действие (читает, добавляет закладку)
2. Изменение сохраняется локально
3. Изменение добавляется в очередь синхронизации
4. При наличии интернета - отправляется на сервер
5. Получаем изменения с других устройств

### Слои приложения

**Presentation Layer** (UI + ViewModel)
- Jetpack Compose UI
- ViewModels для управления состоянием
- LiveData/Flow для реактивности

**Domain Layer** (Use Cases)
- Бизнес-логика
- Независим от Android Framework

**Data Layer** (Repository + DataSource)
- Room Database (локальные данные)
- Retrofit (удаленные данные)
- Repository объединяет источники

## Синхронизация

### Автоматическая синхронизация
- Каждые 15-30 минут (WorkManager)
- При открытии приложения
- При выходе из книги
- При добавлении закладки

### Разрешение конфликтов
- Прогресс чтения: последняя запись побеждает (по timestamp)
- Закладки: объединение (merge)
- Выделения: последнее побеждает

### Настройки синхронизации
- Включить/выключить автосинхронизацию
- Только по WiFi
- Синхронизировать файлы книг

## Backend API (в разработке)

### Endpoints
```
POST   /api/auth/register     - Регистрация
POST   /api/auth/login        - Вход
POST   /api/auth/refresh      - Обновление токена

GET    /api/books             - Получить список книг
POST   /api/books             - Добавить книгу
PUT    /api/books/:id         - Обновить книгу
DELETE /api/books/:id         - Удалить книгу

GET    /api/progress/:bookId  - Получить прогресс
POST   /api/progress          - Синхронизировать прогресс

GET    /api/bookmarks         - Получить закладки
POST   /api/bookmarks         - Синхронизировать закладки

GET    /api/sync              - Получить все изменения
POST   /api/sync              - Отправить изменения
```

## Дорожная карта

### Фаза 1: MVP (Текущая) ✅
- [x] Настройка проекта и зависимостей
- [x] Создание моделей данных (Room entities)
- [x] DAO интерфейсы
- [x] Database настройка
- [x] Repository слой
- [ ] UI экраны (Splash, Login, Library, Reader)
- [ ] ViewModels
- [ ] EPUB парсер
- [ ] Базовая читалка

### Фаза 2: Синхронизация
- [ ] Backend API
- [ ] Retrofit интеграция
- [ ] WorkManager для фоновой синхронизации
- [ ] Разрешение конфликтов

### Фаза 3: Расширенный функционал
- [ ] Статистика чтения
- [ ] Выделения и заметки
- [ ] Поиск по библиотеке
- [ ] PDF поддержка
- [ ] Темы оформления
- [ ] Настройки читалки

### Фаза 4: Полировка и релиз
- [ ] Оптимизация производительности
- [ ] UI/UX улучшения
- [ ] Тестирование
- [ ] Подготовка к Google Play
- [ ] Документация

## Вклад в проект
Приветствуются Pull Requests! Пожалуйста:
1. Создайте форк проекта
2. Создайте ветку для фичи (`git checkout -b feature/AmazingFeature`)
3. Закоммитьте изменения (`git commit -m 'Add some AmazingFeature'`)
4. Запушьте в ветку (`git push origin feature/AmazingFeature`)
5. Откройте Pull Request

## Лицензия
MIT License - см. файл [LICENSE](LICENSE)

## Контакты
- Email: support@kniga.app
- Telegram: @kniga_support

## Статус проекта
🚧 **В активной разработке** 🚧

Текущая версия: 1.0.0-alpha
Последнее обновление: 13 октября 2025
# Kniga
