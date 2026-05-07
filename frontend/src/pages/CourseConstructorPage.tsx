import React, { useEffect, useState, useMemo } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
    getCourseById,
    createModule,
    deleteModule,
    getModulesByCourseId,
    getMyRoleInCourse,
    getAllCourseDependencies,
    createDependency,
    deleteDependency,
} from '../services/courseApi';
import DependencyGraph from '../components/DependencyGraph';

interface Module {
    moduleId: number;
    name: string;
    isOpen: boolean;
}

interface Dependency {
    id: number;
    blockerId: number;
    blockerName: string;
    dependentId: number;
    dependentName: string;
}

const CourseConstructorPage: React.FC = () => {
    const { courseId } = useParams<{ courseId: string }>();
    const navigate = useNavigate();

    const [courseName, setCourseName] = useState('');
    const [courseDescription, setCourseDescription] = useState('');
    const [modules, setModules] = useState<Module[]>([]);
    const [newModuleName, setNewModuleName] = useState('');
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [creating, setCreating] = useState(false);

    const [dependencies, setDependencies] = useState<Dependency[]>([]);
    const [selectedBlocker, setSelectedBlocker] = useState<number>(0);
    const [selectedDependent, setSelectedDependent] = useState<number>(0);
    const [loadingDeps, setLoadingDeps] = useState(false);
    const [viewMode, setViewMode] = useState<'list' | 'graph'>('list');

    useEffect(() => {
        loadCourseAndModules();
    }, [courseId]);

    const loadCourseAndModules = async () => {
        try {
            setLoading(true);
            setError('');

            const [course, modulesData, role] = await Promise.all([
                getCourseById(Number(courseId)),
                getModulesByCourseId(Number(courseId)),
                getMyRoleInCourse(Number(courseId)),
            ]);

            setCourseName(course.name);
            setCourseDescription(course.description);
            setModules(modulesData || []);

            if (role === 'admin') {
                await loadDependencies(modulesData || []);
            } else {
                navigate(`/course/${courseId}`);
            }
        } catch (err: any) {
            console.error(err);
            setError('Ошибка загрузки курса');
        } finally {
            setLoading(false);
        }
    };

    const loadDependencies = async (modulesList: Module[]) => {
        setLoadingDeps(true);
        try {
            const graph = await getAllCourseDependencies(Number(courseId));
            console.log('Raw graph data:', graph);

            const flatDeps: Dependency[] = [];
            for (const [blockerId, blockedList] of Object.entries(graph)) {
                const blockerMod = modulesList.find(m => m.moduleId === Number(blockerId));
                if (!blockerMod) continue;
                for (const item of blockedList as any[]) {
                    flatDeps.push({
                        id: item.id,
                        blockerId: Number(blockerId),
                        blockerName: blockerMod.name,
                        dependentId: item.blockedModuleId,
                        dependentName: item.blockedModuleName,
                    });
                }
            }
            setDependencies(flatDeps);
        } catch (err) {
            console.error('Ошибка загрузки зависимостей', err);
        } finally {
            setLoadingDeps(false);
        }
    };

    const handleCreateModule = async () => {
        if (!newModuleName.trim()) {
            alert('Введите название модуля');
            return;
        }
        try {
            setCreating(true);
            await createModule(Number(courseId), newModuleName, false);
            setNewModuleName('');
            await loadCourseAndModules();
        } catch (err: any) {
            alert(err.response?.data || 'Ошибка создания модуля');
        } finally {
            setCreating(false);
        }
    };

    const handleDeleteModule = async (moduleId: number) => {
        if (!confirm('Удалить модуль? Все задачи и зависимости тоже удалятся.')) return;
        try {
            await deleteModule(moduleId);
            await loadCourseAndModules();
        } catch (err: any) {
            alert(err.response?.data || 'Ошибка удаления модуля');
        }
    };

    const handleAddDependency = async () => {
        if (selectedBlocker === selectedDependent) {
            alert('Модуль не может блокировать сам себя');
            return;
        }
        if (selectedBlocker === 0 || selectedDependent === 0) {
            alert('Выберите оба модуля');
            return;
        }
        try {
            const success = await createDependency(selectedBlocker, selectedDependent);
            if (success) {
                alert('Зависимость добавлена');
                setSelectedBlocker(0);
                setSelectedDependent(0);
                await loadDependencies(modules);
            } else {
                alert('Не удалось добавить зависимость (цикл или уже существует)');
            }
        } catch (err: any) {
            console.error(err);
            alert(err.response?.data || 'Ошибка создания зависимости');
        }
    };

    const handleDeleteDependency = async (depId: number) => {
        if (!confirm('Удалить зависимость?')) return;
        try {
            await deleteDependency(depId);
            await loadDependencies(modules);
        } catch (err) {
            alert('Ошибка удаления зависимости');
        }
    };

    const handleModuleClick = (moduleId: number) => {
        navigate(`/module/${moduleId}?courseId=${courseId}`);
    };

    const handleGraphNodeClick = (nodeId: number) => {
        navigate(`/module/${nodeId}?courseId=${courseId}`);
    };

    const graphModules = useMemo(() => modules
            .filter(m => m && m.moduleId)
            .map(m => ({ id: m.moduleId, name: m.name })),
        [modules]
    );

    const graphEdges = useMemo(() => dependencies
            .filter(d => d && d.id && d.blockerId && d.dependentId)
            .map(d => ({ id: d.id, from: d.blockerId, to: d.dependentId })),
        [dependencies]
    );

    if (loading) return <div style={{ padding: '20px' }}>Загрузка...</div>;
    if (error) {
        return (
            <div style={{ padding: '20px' }}>
                <p style={{ color: 'red' }}>{error}</p>
                <button onClick={() => navigate('/my-courses')}>Назад к курсам</button>
            </div>
        );
    }

    return (
        <div style={{ padding: '20px', maxWidth: '1200px', margin: '0 auto' }}>
            <button onClick={() => navigate('/my-courses')} style={{ marginBottom: '16px' }}>
                ← Назад к курсам
            </button>

            <h1>{courseName}</h1>
            {courseDescription && (
                <p style={{ color: '#555', marginBottom: '20px', lineHeight: '1.6' }}>
                    {courseDescription}
                </p>
            )}

            <div style={{ marginBottom: '24px', padding: '16px', border: '1px solid #ddd', borderRadius: '8px' }}>
                <h3 style={{ margin: '0 0 8px 0' }}>Создать новый модуль</h3>
                <div style={{ display: 'flex', gap: '8px' }}>
                    <input
                        type="text"
                        placeholder="Название модуля"
                        value={newModuleName}
                        onChange={(e) => setNewModuleName(e.target.value)}
                        style={{ flex: 1, padding: '8px' }}
                        disabled={creating}
                    />
                    <button onClick={handleCreateModule} disabled={creating}>
                        {creating ? 'Создание...' : '+ Добавить модуль'}
                    </button>
                </div>
            </div>

            <h2>Модули курса</h2>
            {modules.length === 0 ? (
                <p>В курсе пока нет модулей. Создайте первый модуль!</p>
            ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                    {modules.map(module => (
                        <div
                            key={module.moduleId}
                            style={{
                                border: '1px solid #ddd',
                                borderRadius: '8px',
                                padding: '16px',
                                display: 'flex',
                                justifyContent: 'space-between',
                                alignItems: 'center',
                                transition: 'box-shadow 0.2s',
                                cursor: 'pointer',
                            }}
                            onMouseEnter={(e) => (e.currentTarget.style.boxShadow = '0 2px 8px rgba(0,0,0,0.1)')}
                            onMouseLeave={(e) => (e.currentTarget.style.boxShadow = 'none')}
                            onClick={() => handleModuleClick(module.moduleId)}
                        >
                            <div style={{ flex: 1 }}>
                                <strong>{module.name}</strong>
                            </div>
                            <button
                                onClick={(e) => { e.stopPropagation(); handleDeleteModule(module.moduleId); }}
                                style={{
                                    background: '#dc3545',
                                    color: 'white',
                                    border: 'none',
                                    padding: '6px 12px',
                                    borderRadius: '4px',
                                    cursor: 'pointer',
                                    marginLeft: '12px',
                                }}
                            >
                                Удалить
                            </button>
                        </div>
                    ))}
                </div>
            )}

            <div style={{ marginTop: '32px', borderTop: '2px solid #ddd', paddingTop: '20px' }}>
                <h2>Управление зависимостями модулей</h2>
                <div style={{ marginBottom: '24px', padding: '16px', border: '1px solid #ddd', borderRadius: '8px' }}>
                    <h3>Добавить зависимость</h3>
                    <p style={{ fontSize: '14px', color: '#666' }}>Блокирующий модуль → блокирует → зависимый модуль</p>
                    <div style={{ display: 'flex', gap: '16px', alignItems: 'flex-end', flexWrap: 'wrap' }}>
                        <div style={{ flex: 1 }}>
                            <label>Блокирующий модуль:</label>
                            <select
                                value={selectedBlocker}
                                onChange={e => setSelectedBlocker(Number(e.target.value))}
                                style={{ width: '100%', padding: '8px', marginTop: '4px' }}
                            >
                                <option value={0}>-- выберите --</option>
                                {modules.map(m => (
                                    <option key={m.moduleId} value={m.moduleId}>{m.name}</option>
                                ))}
                            </select>
                        </div>
                        <div style={{ flex: 1 }}>
                            <label>Зависимый модуль (который блокируется):</label>
                            <select
                                value={selectedDependent}
                                onChange={e => setSelectedDependent(Number(e.target.value))}
                                style={{ width: '100%', padding: '8px', marginTop: '4px' }}
                            >
                                <option value={0}>-- выберите --</option>
                                {modules.map(m => (
                                    <option key={m.moduleId} value={m.moduleId}>{m.name}</option>
                                ))}
                            </select>
                        </div>
                        <button onClick={handleAddDependency}>Добавить</button>
                    </div>
                </div>

                <div style={{ marginBottom: '16px' }}>
                    <button
                        onClick={() => setViewMode('list')}
                        style={{
                            background: viewMode === 'list' ? '#007bff' : '#f0f0f0',
                            color: viewMode === 'list' ? 'white' : '#333',
                            marginRight: '8px',
                            padding: '6px 12px',
                            borderRadius: '4px',
                            border: 'none',
                            cursor: 'pointer',
                        }}
                    >
                        Список зависимостей
                    </button>
                    <button
                        onClick={() => setViewMode('graph')}
                        style={{
                            background: viewMode === 'graph' ? '#007bff' : '#f0f0f0',
                            color: viewMode === 'graph' ? 'white' : '#333',
                            padding: '6px 12px',
                            borderRadius: '4px',
                            border: 'none',
                            cursor: 'pointer',
                        }}
                    >
                        Граф зависимостей
                    </button>
                </div>

                {viewMode === 'list' && (
                    <>
                        <h3>Существующие зависимости</h3>
                        {loadingDeps && <p>Загрузка...</p>}
                        {!loadingDeps && dependencies.length === 0 && <p>Нет зависимостей</p>}
                        {!loadingDeps && dependencies.length > 0 && (
                            <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                                {dependencies.map(dep => (
                                    <div
                                        key={dep.id}
                                        style={{
                                            border: '1px solid #ddd',
                                            borderRadius: '6px',
                                            padding: '12px',
                                            display: 'flex',
                                            justifyContent: 'space-between',
                                            alignItems: 'center',
                                        }}
                                    >
                                        <span>
                                            <strong>{dep.blockerName}</strong> → блокирует → <strong>{dep.dependentName}</strong>
                                        </span>
                                        <button
                                            onClick={() => handleDeleteDependency(dep.id)}
                                            style={{
                                                background: '#dc3545',
                                                color: 'white',
                                                border: 'none',
                                                padding: '4px 12px',
                                                borderRadius: '4px',
                                            }}
                                        >
                                            Удалить
                                        </button>
                                    </div>
                                ))}
                            </div>
                        )}
                    </>
                )}

                {viewMode === 'graph' && (
                    <>
                        <h3>Граф зависимостей</h3>
                        {loadingDeps && <p>Загрузка...</p>}
                        {!loadingDeps && modules.length > 0 && graphEdges.length > 0 && (
                            <DependencyGraph
                                modules={graphModules}
                                dependencies={graphEdges}
                                onNodeClick={handleGraphNodeClick}
                            />
                        )}
                        {!loadingDeps && modules.length > 0 && graphEdges.length === 0 && (
                            <p>Нет зависимостей для отображения в графе</p>
                        )}
                        {!loadingDeps && modules.length === 0 && <p>Нет модулей для отображения графа</p>}
                    </>
                )}
                <button onClick={() => loadDependencies(modules)} style={{ marginTop: '16px' }}>Обновить данные</button>
            </div>
        </div>
    );
};

export default CourseConstructorPage;