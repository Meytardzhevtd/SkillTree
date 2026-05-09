import React, { useEffect, useMemo } from 'react';
import ReactFlow, {
    Background,
    Controls,
    MiniMap,
    useNodesState,
    useEdgesState,
    MarkerType,
} from 'reactflow';
import type { Node } from 'reactflow';
import 'reactflow/dist/style.css';

interface ModuleNode {
    id: number;
    name: string;
    x?: number;
    y?: number;
}

interface DependencyEdge {
    id: number;
    from: number;
    to: number;
}

interface DependencyGraphProps {
    modules: ModuleNode[];
    dependencies: DependencyEdge[];
    onNodeClick?: (nodeId: number) => void;
    onNodeDragStop?: (nodeId: number, position: { x: number; y: number }) => void;
    readOnly?: boolean;
    getNodeStyle?: (nodeId: number) => React.CSSProperties;
}

const DependencyGraph: React.FC<DependencyGraphProps> = ({
                                                             modules,
                                                             dependencies,
                                                             onNodeClick,
                                                             onNodeDragStop,
                                                             readOnly = false,
                                                             getNodeStyle,
                                                         }) => {
    const safeModules = Array.isArray(modules) ? modules : [];
    const safeDependencies = Array.isArray(dependencies) ? dependencies : [];

    const initialNodes = useMemo(() => {
        return safeModules
            .filter(mod => mod && mod.id && mod.name)
            .map((mod, index) => {
                const baseStyle = {
                    background: '#f0f9ff',
                    border: '1px solid #007bff',
                    borderRadius: '8px',
                    padding: '10px',
                    width: 180,
                };
                const customStyle = getNodeStyle ? getNodeStyle(mod.id) : {};
                const x = mod.x !== undefined ? mod.x : (index % 3) * 250;
                const y = mod.y !== undefined ? mod.y : Math.floor(index / 3) * 150;
                return {
                    id: mod.id.toString(),
                    data: { label: mod.name },
                    position: { x, y },
                    style: { ...baseStyle, ...customStyle },
                    draggable: !readOnly,
                };
            });
    }, [safeModules, getNodeStyle, readOnly]);

    const initialEdges = useMemo(() => {
        return safeDependencies
            .filter(dep => dep && dep.id && dep.from && dep.to)
            .map((dep) => ({
                id: `edge-${dep.id}`,
                source: dep.from.toString(),
                target: dep.to.toString(),
                type: 'smoothstep',
                markerEnd: { type: MarkerType.ArrowClosed },
            }));
    }, [safeDependencies]);

    const [nodes, setNodes, onNodesChange] = useNodesState(initialNodes);
    const [edges, setEdges, onEdgesChange] = useEdgesState(initialEdges);

    useEffect(() => {
        setNodes(initialNodes);
    }, [initialNodes, setNodes]);

    useEffect(() => {
        setEdges(initialEdges);
    }, [initialEdges, setEdges]);

    const handleNodeClick = (_event: React.MouseEvent, node: Node) => {
        if (onNodeClick) {
            onNodeClick(Number(node.id));
        }
    };

    const handleNodeDragStop = (_event: React.MouseEvent, node: Node) => {
        if (onNodeDragStop && !readOnly) {
            onNodeDragStop(Number(node.id), node.position);
        }
    };

    if (initialNodes.length === 0) {
        return <div style={{ padding: '20px', textAlign: 'center', color: '#888' }}>Нет модулей для отображения графа</div>;
    }

    return (
        <div style={{ width: '100%', height: '500px', border: '1px solid #ddd', borderRadius: '8px' }}>
            <ReactFlow
                nodes={nodes}
                edges={edges}
                onNodesChange={onNodesChange}
                onEdgesChange={onEdgesChange}
                onNodeClick={handleNodeClick}
                onNodeDragStop={handleNodeDragStop}
                nodesDraggable={!readOnly}
                fitView
                attributionPosition="bottom-right"
            >
                <Background />
                <Controls />
                <MiniMap />
            </ReactFlow>
        </div>
    );
};

export default DependencyGraph;