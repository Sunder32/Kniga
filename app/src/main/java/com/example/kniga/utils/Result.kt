package com.example.kniga.utils

/**
 * Обертка для результата операции (Success/Error)
 */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Exception, val message: String? = null) : Result<Nothing>()
    object Loading : Result<Nothing>()
    
    val isSuccess: Boolean
        get() = this is Success
    
    val isError: Boolean
        get() = this is Error
    
    val isLoading: Boolean
        get() = this is Loading
    
    /**
     * Получить данные или null
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }
    
    /**
     * Получить данные или значение по умолчанию
     */
    fun getOrDefault(default: @UnsafeVariance T): T = when (this) {
        is Success -> data
        else -> default
    }
    
    /**
     * Выполнить действие если успех
     */
    inline fun onSuccess(action: (T) -> Unit): Result<T> {
        if (this is Success) {
            action(data)
        }
        return this
    }
    
    /**
     * Выполнить действие если ошибка
     */
    inline fun onError(action: (Exception, String?) -> Unit): Result<T> {
        if (this is Error) {
            action(exception, message)
        }
        return this
    }
    
    /**
     * Выполнить действие если загрузка
     */
    inline fun onLoading(action: () -> Unit): Result<T> {
        if (this is Loading) {
            action()
        }
        return this
    }
    
    companion object {
        /**
         * Обернуть операцию в Result
         */
        inline fun <T> of(block: () -> T): Result<T> {
            return try {
                Success(block())
            } catch (e: Exception) {
                Error(e, e.message)
            }
        }
    }
}

/**
 * Преобразовать Result<T> в Result<R>
 */
inline fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> {
    return when (this) {
        is Result.Success -> Result.Success(transform(data))
        is Result.Error -> Result.Error(exception, message)
        is Result.Loading -> Result.Loading
    }
}

/**
 * Преобразовать Result<T> в Result<R> с возможностью ошибки
 */
inline fun <T, R> Result<T>.flatMap(transform: (T) -> Result<R>): Result<R> {
    return when (this) {
        is Result.Success -> transform(data)
        is Result.Error -> Result.Error(exception, message)
        is Result.Loading -> Result.Loading
    }
}
