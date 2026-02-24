import { useState } from 'react'
import type { FormEvent } from 'react'

type RegisterFormState = {
    username: string
    email: string
    password: string
}

function RegisterPage() {
    const [form, setForm] = useState<RegisterFormState>({
        username: '',
        email: '',
        password: '',
    })

    const [isSubmitting, setIsSubmitting] = useState(false)
    const [successMessage, setSuccessMessage] = useState('')
    const [errorMessage, setErrorMessage] = useState('')
    const handleInputChange = (field: keyof RegisterFormState, value: string) => {
        setForm((prev) => ({ ...prev, [field]: value }))
    }

    const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
        event.preventDefault()
        setSuccessMessage('')
        setErrorMessage('')
        setIsSubmitting(true)

        try {
            const response = await fetch('/api/auth/register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    username: form.username,
                    email: form.email,
                    password: form.password,
                }),
            })

            const message = await response.text()

            if (!response.ok) {
                setErrorMessage(message || 'Ошибка регистрации')
                return
            }

            setSuccessMessage(message || 'Регистрация прошла успешно')

            setForm({ username: '', email: '', password: '' })
        } catch {
            setErrorMessage('Не удалось отправить запрос. Проверь, запущен ли backend на :8080')
        } finally {
            setIsSubmitting(false)
        }
    }

    return (
        <div style={{ maxWidth: '420px', margin: '40px auto', padding: '16px' }}>
            <h1>Регистрация</h1>

            <form onSubmit={handleSubmit}>
                <div style={{ marginBottom: '12px' }}>
                    <label htmlFor="username">Имя пользователя</label>
                    <br />
                    <input
                        id="username"
                        name="username"
                        type="text"
                        placeholder="Введите username"
                        value={form.username}
                        onChange={(event) => handleInputChange('username', event.target.value)}
                        required
                    />
                </div>

                <div style={{ marginBottom: '12px' }}>
                    <label htmlFor="email">Email</label>
                    <br />
                    <input
                        id="email"
                        name="email"
                        type="email"
                        placeholder="Введите email"
                        value={form.email}
                        onChange={(event) => handleInputChange('email', event.target.value)}
                        required
                    />
                </div>

                <div style={{ marginBottom: '12px' }}>
                    <label htmlFor="password">Пароль</label>
                    <br />
                    <input
                        id="password"
                        name="password"
                        type="password"
                        placeholder="Введите пароль"
                        value={form.password}
                        onChange={(event) => handleInputChange('password', event.target.value)}
                        required
                    />
                </div>

                <button type="submit" disabled={isSubmitting}>
                    {isSubmitting ? 'Отправка...' : 'Зарегистрироваться'}
                </button>
            </form>

            {successMessage && <p style={{ color: 'green', marginTop: '12px' }}>{successMessage}</p>}
            {errorMessage && <p style={{ color: 'crimson', marginTop: '12px' }}>{errorMessage}</p>}
        </div>
    )
}

export default RegisterPage
