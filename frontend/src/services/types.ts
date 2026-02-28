export interface TaskDto {
  id: number;
  content: string;
}

export interface ModuleDto {
  id: number;
  name: string;
  tasks: TaskDto[];
}

export interface CourseDto {
  id: number;
  name: string;
  description: string;
  modules: ModuleDto[];
}