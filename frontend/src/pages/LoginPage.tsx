import { useState } from 'react'
import type { FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { loginUser } from '../services/authApi.ts'
import { saveAccessToken } from '../services/authStorage'

type LoginFormState = {
    email: string
    password: string
}

function LoginPage() {
    const navigate = useNavigate()

    const [form, setForm] = useState<LoginFormState>({
        email: '',
        password: '',
    })

    // true пока ждём ответ от backend.
    const [isSubmitting, setIsSubmitting] = useState(false)

    const [successMessage, setSuccessMessage] = useState('')
    const [errorMessage, setErrorMessage] = useState('')

    const handleInputChange = (field: keyof LoginFormState, value: string) => {
        setForm((prev) => ({ ...prev, [field]: value }))
    }

    const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
        event.preventDefault()

        setSuccessMessage('')
        setErrorMessage('')
        setIsSubmitting(true)

        try {
            const authData = await loginUser({
                email: form.email,
                password: form.password,
            })

            // Сохраняем JWT через отдельный helper (единая точка хранения токена).
            saveAccessToken(authData.token)
            setSuccessMessage('Вход выполнен. JWT сохранён в localStorage.')
            setForm((prev) => ({ ...prev, password: '' }))

            // После успешного входа отправляем пользователя в защищённый личный кабинет.
            navigate('/dashboard')
        } catch (error) {
            if (error instanceof Error) {
                setErrorMessage(error.message)
            } else {
                setErrorMessage('Ошибка входа')
            }
        } finally {
            setIsSubmitting(false)
        }
    }

    return (
        <div style={{ maxWidth: '420px', margin: '40px auto', padding: '16px' }}>
            <h1>Вход</h1>

            <form onSubmit={handleSubmit}>
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
                    {isSubmitting ? 'Входим...' : 'Войти'}
                </button>
            </form>

            {successMessage && <p style={{ color: 'green', marginTop: '12px' }}>{successMessage}</p>}
            {errorMessage && <p style={{ color: 'crimson', marginTop: '12px' }}>{errorMessage}</p>}
        </div>
    )
}

export default LoginPage
