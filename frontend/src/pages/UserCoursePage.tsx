import React, { useEffect, useState } from 'react';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import {
    getCourseById,
    getModulesByCourseId,
    getMyRoleInCourse,
    getMyTakenCourses,
    getStudentDependencyGraph,
} from '../services/courseApi';
import DependencyGraph from '../components/DependencyGraph';

interface Module {
    moduleId: number;
    name: string;
    isOpen: boolean;
    x?: number | null;
    y?: number | null;
}

interface ModuleProgress {
    moduleId: number;
    moduleName: string;
    progress: number;
}

const UserCoursePage: React.FC = () => {
    const { courseId } = useParams<{ courseId: string }>();
    const navigate = useNavigate();
    const location = useLocation();

    const [courseName, setCourseName] = useState('');
    const [courseDescription, setCourseDescription] = useState('');
    const [modules, setModules] = useState<Module[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [courseProgress, setCourseProgress] = useState<number | null>(null);
    const [moduleProgresses, setModuleProgresses] = useState<ModuleProgress[]>([]);

    const [studentDepsMap, setStudentDepsMap] = useState<Map<number, any[]>>(new Map());
    const [studentViewMode, setStudentViewMode] = useState<'list' | 'graph'>('list');

    const [takenCourseId, setTakenCourseId] = useState<number | null>(null);

    const loadCourseAndModules = async () => {
        try {
            setLoading(true);
            setError('');

            const [course, modulesData, role] = await Promise.all([
                getCourseById(Number(courseId)),
                getModulesByCourseId(Number(courseId)),
                getMyRoleInCourse(Number(courseId)),
            ]);

            setModules(modulesData || []);
            setCourseName(course.name);
            setCourseDescription(course.description);

            if (role !== 'student') {
                navigate(`/course/${courseId}/edit`);
                return;
            }

            const takenCourses = await getMyTakenCourses();
            const tc = takenCourses.find((t: any) => t.courseId === Number(courseId));
            if (tc) {
                setCourseProgress(tc.progress);
                setModuleProgresses(tc.moduleProgresses || []);
                setTakenCourseId(tc.takenCourseId);
                const graphData = await getStudentDependencyGraph(tc.takenCourseId);
                console.log('📊 Graph data:', graphData);
                const depsMap = new Map<number, any[]>(
                    Object.entries(graphData).map(([key, val]) => [Number(key), val as any[]])
                );
                setStudentDepsMap(depsMap);
                const updatedModules = updateModulesOpenStatus(modulesData, depsMap);
                setModules(updatedModules);
            } else {
                setModules(modulesData || []);
            }
        } catch (err: any) {
            console.error(err);
            setError('Ошибка загрузки курса');
        } finally {
            setLoading(false);
        }
    };

    const updateModulesOpenStatus = (modulesList: Module[], depsMap: Map<number, any[]>): Module[] => {
        const openStatus = new Map<number, boolean>();
        modulesList.forEach(m => openStatus.set(m.moduleId, false));

        const dependentIds = new Set<number>();
        for (const items of depsMap.values()) {
            for (const item of items) {
                const dependentId = item.blockedModuleId;
                dependentIds.add(dependentId);
                openStatus.set(dependentId, item.open);
            }
        }
        for (const m of modulesList) {
            if (!dependentIds.has(m.moduleId)) {
                openStatus.set(m.moduleId, true);
            }
        }

        console.log('DepsMap:', depsMap);
        for (let [blockerId, items] of depsMap.entries()) {
            console.log(`Blocker ${blockerId}:`, items);
        }

        return modulesList.map(m => ({ ...m, isOpen: openStatus.get(m.moduleId) || false }));
    };

    useEffect(() => {
        loadCourseAndModules();
    }, [courseId, location.pathname, location.search]);

    const getModuleProgress = (moduleId: number): number | null => {
        const mp = moduleProgresses.find(m => m.moduleId === moduleId);
        return mp ? mp.progress : null;
    };

    const handleModuleClick = (moduleId: number, isOpen: boolean) => {
        if (!isOpen) {
            alert('Этот модуль пока недоступен. Сначала пройдите блокирующие модули.');
            return;
        }
        const params = new URLSearchParams();
        params.set('courseId', courseId!);
        if (takenCourseId) params.set('takenCourseId', takenCourseId.toString());
        navigate(`/module/${moduleId}?${params.toString()}`);
    };

    const handleGraphNodeClick = (nodeId: number) => {
        const module = modules.find(m => m.moduleId === nodeId);
        if (!module) return;
        if (!module.isOpen) {
            alert('Этот модуль пока недоступен. Сначала пройдите блокирующие модули.');
            return;
        }
        const params = new URLSearchParams();
        params.set('courseId', courseId!);
        if (takenCourseId) params.set('takenCourseId', takenCourseId.toString());
        navigate(`/module/${nodeId}?${params.toString()}`);
    };

    const getNodeStyle = (nodeId: number): React.CSSProperties => {
        const module = modules.find(m => m.moduleId === nodeId);
        if (!module) return {};
        const progress = getModuleProgress(nodeId);
        if (progress === 100) {
            return { background: '#d4edda', border: '2px solid #28a745' };
        }
        if (module.isOpen) {
            return { background: '#f0f9ff', border: '1px solid #007bff' };
        }
        return { background: '#e9ecef', border: '1px solid #ced4da', color: '#6c757d' };
    };

    const studentGraphModules = modules.map(m => ({
        id: m.moduleId,
        name: m.name,
        x: m.x ?? undefined,
        y: m.y ?? undefined,
    }));
    const studentGraphEdges = (() => {
        const edges: { id: number; from: number; to: number }[] = [];
        for (const [blockerId, items] of studentDepsMap.entries()) {
            for (const item of items) {
                edges.push({
                    id: item.id,
                    from: blockerId,
                    to: item.blockedModuleId,
                });
            }
        }
        return edges;
    })();

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

            {courseProgress !== null && (
                <div style={{ marginBottom: '24px', padding: '16px', background: '#f8f9fa', borderRadius: '8px', border: '1px solid #e9ecef' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '6px' }}>
                        <span style={{ fontSize: '14px', fontWeight: 'bold', color: '#333' }}>Прогресс курса</span>
                        <span style={{ fontSize: '14px', color: '#555' }}>{courseProgress.toFixed(0)}%</span>
                    </div>
                    <div style={{ background: '#e9ecef', borderRadius: '4px', height: '10px', overflow: 'hidden' }}>
                        <div
                            style={{
                                background: courseProgress === 100 ? '#28a745' : '#007bff',
                                height: '100%',
                                width: `${courseProgress}%`,
                                transition: 'width 0.3s',
                            }}
                        />
                    </div>
                </div>
            )}

            <button
                onClick={() => loadCourseAndModules()}
                style={{ marginBottom: '16px', background: '#6c757d', color: 'white', border: 'none', padding: '4px 12px', borderRadius: '4px' }}
            >
                🔄 Обновить данные
            </button>

            <div style={{ marginBottom: '16px', display: 'flex', gap: '8px' }}>
                <button
                    onClick={() => setStudentViewMode('list')}
                    style={{
                        background: studentViewMode === 'list' ? '#007bff' : '#f0f0f0',
                        color: studentViewMode === 'list' ? 'white' : '#333',
                        padding: '6px 12px',
                        borderRadius: '4px',
                        border: 'none',
                        cursor: 'pointer',
                    }}
                >
                    Список модулей
                </button>
                <button
                    onClick={() => setStudentViewMode('graph')}
                    style={{
                        background: studentViewMode === 'graph' ? '#007bff' : '#f0f0f0',
                        color: studentViewMode === 'graph' ? 'white' : '#333',
                        padding: '6px 12px',
                        borderRadius: '4px',
                        border: 'none',
                        cursor: 'pointer',
                    }}
                >
                    Граф зависимостей
                </button>
            </div>

            {studentViewMode === 'list' && (
                <>
                    <h2>Модули курса</h2>
                    {modules.length === 0 ? (
                        <p>В курсе пока нет модулей.</p>
                    ) : (
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                            {modules.map(module => {
                                const progress = getModuleProgress(module.moduleId);
                                return (
                                    <div
                                        key={module.moduleId}
                                        style={{
                                            border: '1px solid #ddd',
                                            borderRadius: '8px',
                                            padding: '16px',
                                            display: 'flex',
                                            alignItems: 'center',
                                            transition: 'box-shadow 0.2s',
                                            cursor: 'pointer',
                                        }}
                                        onMouseEnter={e => (e.currentTarget.style.boxShadow = '0 2px 8px rgba(0,0,0,0.1)')}
                                        onMouseLeave={e => (e.currentTarget.style.boxShadow = 'none')}
                                    >
                                        <div onClick={() => handleModuleClick(module.moduleId, module.isOpen)} style={{ flex: 1 }}>
                                            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                                                <strong>{module.name}</strong>
                                                {!module.isOpen && <span style={{ color: '#888' }}>🔒</span>}
                                                {progress === 100 && (
                                                    <span style={{ fontSize: '12px', background: '#28a745', color: 'white', padding: '2px 8px', borderRadius: '4px' }}>
                                                        ✓ Завершён
                                                    </span>
                                                )}
                                            </div>
                                            {progress !== null && (
                                                <div style={{ marginTop: '8px' }}>
                                                    <div style={{ background: '#e9ecef', borderRadius: '4px', height: '6px', overflow: 'hidden' }}>
                                                        <div
                                                            style={{
                                                                background: progress === 100 ? '#28a745' : '#007bff',
                                                                height: '100%',
                                                                width: `${progress}%`,
                                                                transition: 'width 0.3s',
                                                            }}
                                                        />
                                                    </div>
                                                    <span style={{ fontSize: '12px', color: '#888' }}>{progress.toFixed(0)}%</span>
                                                </div>
                                            )}
                                        </div>
                                    </div>
                                );
                            })}
                        </div>
                    )}
                </>
            )}

            {studentViewMode === 'graph' && (
                <div>
                    <h3>Граф зависимостей модулей</h3>
                    {studentGraphModules.length === 0 ? (
                        <p>Нет модулей для отображения графа</p>
                    ) : (
                        <DependencyGraph
                            modules={studentGraphModules}
                            dependencies={studentGraphEdges}
                            onNodeClick={handleGraphNodeClick}
                            getNodeStyle={getNodeStyle}
                            readOnly={true}
                        />
                    )}
                </div>
            )}
        </div>
    );
};

export default UserCoursePage;