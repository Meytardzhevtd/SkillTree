import React, { useEffect, useMemo } from 'react';
import ReactFlow, {
    Background,
    Controls,
    MiniMap,
    useNodesState,
    useEdgesState,
    MarkerType,
} from 'reactflow';
import 'reactflow/dist/style.css';

interface ModuleNode {
    id: number;
    name: string;
}

interface DependencyEdge {
    id: number;
    from: number;
    to: number;
}

interface DependencyGraphProps {
    modules: ModuleNode[];
    dependencies: DependencyEdge[];
}

const DependencyGraph: React.FC<DependencyGraphProps> = ({ modules, dependencies }) => {
    const initialNodes = useMemo(() => {
        return modules.map((mod, index) => ({
            id: mod.id.toString(),
            data: { label: mod.name },
            position: { x: (index % 3) * 250, y: Math.floor(index / 3) * 150 },
            style: { background: '#f0f9ff', border: '1px solid #007bff', borderRadius: '8px', padding: '10px', width: 180 },
        }));
    }, [modules]);

    const initialEdges = useMemo(() => {
        return dependencies.map((dep) => ({
            id: `edge-${dep.id}`,
            source: dep.from.toString(),
            target: dep.to.toString(),
            type: 'smoothstep',
            markerEnd: { type: MarkerType.ArrowClosed },
        }));
    }, [dependencies]);

    const [nodes, setNodes, onNodesChange] = useNodesState(initialNodes);
    const [edges, setEdges, onEdgesChange] = useEdgesState(initialEdges);

    useEffect(() => {
        setNodes(initialNodes);
    }, [initialNodes, setNodes]);

    useEffect(() => {
        setEdges(initialEdges);
    }, [initialEdges, setEdges]);

    return (
        <div style={{ width: '100%', height: '500px', border: '1px solid #ddd', borderRadius: '8px' }}>
            <ReactFlow
                nodes={nodes}
                edges={edges}
                onNodesChange={onNodesChange}
                onEdgesChange={onEdgesChange}
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