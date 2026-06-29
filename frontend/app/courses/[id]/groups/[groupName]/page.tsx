import RegisteredStudentTable from "@components/groups/registeredStudentTable";
import CourseService from "@services/CourseService";
import { Metadata } from "next";
/*
type Props = {
  params: { id: string; groupName: string } | Promise<{ id: string; groupName: string }>;
};*/

type Params = {
  id: string;
  groupName: string;
};

export async function generateMetadata({ params }: { params: Promise<{id: string; groupName: string;}> }): Promise<Metadata> {
    const { id, groupName} = await (params)
  return {
    title: `Students in ${decodeURI(groupName)}`,
  };
}

export default async function GroupPage({ params }: { params: Promise<{id: string; groupName: string;}> }) {
    const { id, groupName} = await (params)
    const courseId = parseInt(id);
    const students = await CourseService.getStudentsByGroup(courseId, groupName);

    return <>
        <div className="p-4">
            <h1 className="text-2xl font-bold mb-4">
                Students in {decodeURIComponent(groupName)}:
            </h1>

            <RegisteredStudentTable
                courseId={courseId}
                groupName={groupName}
                initialStudents={students}
            />
        </div>
    </>;
}