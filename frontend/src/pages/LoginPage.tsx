import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { loginUser, registerUser } from '../services/authApi';

const LoginPage: React.FC = () => {
    const [activeTab, setActiveTab] = useState<'login' | 'register'>('login');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [username, setUsername] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const handleLogin = async (e: React.FormEvent) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        try {
            await loginUser({ email, password });
            navigate('/dashboard');
        } catch (err: any) {
            setError(err.message || 'Неверный email или пароль');
        } finally {
            setLoading(false);
        }
    };

    const handleRegister = async (e: React.FormEvent) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        try {
            await registerUser({ username, email, password });
            await loginUser({ email, password });
            navigate('/dashboard');
        } catch (err: any) {
            setError(err.message || 'Ошибка регистрации');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={{ maxWidth: '400px', margin: '50px auto', padding: '20px' }}>
            <h1 style={{ textAlign: 'center' }}>SkillTree</h1>

            <div style={{ display: 'flex', marginBottom: '20px', borderBottom: '1px solid #ddd' }}>
                <button
                    onClick={() => setActiveTab('login')}
                    style={{
                        flex: 1,
                        padding: '10px',
                        border: 'none',
                        background: activeTab === 'login' ? '#007bff' : 'transparent',
                        color: activeTab === 'login' ? 'white' : '#333',
                        cursor: 'pointer',
                        borderRadius: '4px 4px 0 0',
                    }}
                >
                    Вход
                </button>
                <button
                    onClick={() => setActiveTab('register')}
                    style={{
                        flex: 1,
                        padding: '10px',
                        border: 'none',
                        background: activeTab === 'register' ? '#007bff' : 'transparent',
                        color: activeTab === 'register' ? 'white' : '#333',
                        cursor: 'pointer',
                        borderRadius: '4px 4px 0 0',
                    }}
                >
                    Регистрация
                </button>
            </div>

            {activeTab === 'login' ? (
                <form onSubmit={handleLogin}>
                    <div style={{ marginBottom: '16px' }}>
                        <label>Email</label>
                        <br />
                        <input
                            type="email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            required
                            style={{ width: '100%', padding: '8px', marginTop: '4px' }}
                        />
                    </div>
                    <div style={{ marginBottom: '16px' }}>
                        <label>Пароль</label>
                        <br />
                        <input
                            type="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            required
                            style={{ width: '100%', padding: '8px', marginTop: '4px' }}
                        />
                    </div>
                    {error && <p style={{ color: 'red' }}>{error}</p>}
                    <button
                        type="submit"
                        disabled={loading}
                        style={{ width: '100%', padding: '10px', marginTop: '8px' }}
                    >
                        {loading ? 'Вход...' : 'Войти'}
                    </button>
                </form>
            ) : (
                <form onSubmit={handleRegister}>
                    <div style={{ marginBottom: '16px' }}>
                        <label>Имя пользователя</label>
                        <br />
                        <input
                            type="text"
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                            required
                            style={{ width: '100%', padding: '8px', marginTop: '4px' }}
                        />
                    </div>
                    <div style={{ marginBottom: '16px' }}>
                        <label>Email</label>
                        <br />
                        <input
                            type="email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            required
                            style={{ width: '100%', padding: '8px', marginTop: '4px' }}
                        />
                    </div>
                    <div style={{ marginBottom: '16px' }}>
                        <label>Пароль</label>
                        <br />
                        <input
                            type="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            required
                            style={{ width: '100%', padding: '8px', marginTop: '4px' }}
                        />
                    </div>
                    {error && <p style={{ color: 'red' }}>{error}</p>}
                    <button
                        type="submit"
                        disabled={loading}
                        style={{ width: '100%', padding: '10px', marginTop: '8px' }}
                    >
                        {loading ? 'Регистрация...' : 'Зарегистрироваться'}
                    </button>
                </form>
            )}
        </div>
    );
};

export default LoginPage;