import GroupOverviewClient from "@components/groups/groupOverviewClient";
import CourseService from "@services/CourseService";
import { GroupWithCount } from "@types";
import { fetchCourseByCourseIdAction } from "actions/courseActions";
import { Metadata } from "next";

export async function generateMetadata({params}: {params:Promise<{ id: string }>}): Promise<Metadata> {
  const { id } = await params;
  const courseId = Number(id);
  const res = await fetchCourseByCourseIdAction(courseId);

  return {
    title: `Groups in ${res.name}`,
  };
}

const GroupOverview = async ({params}: {params:Promise<{ id: string }>}) => {
    const { id } = await params;
    const courseId = Number(id);

    const groups: GroupWithCount[] = await CourseService.getGroupsByCourseId(courseId);

    return <GroupOverviewClient initialGroups={groups} courseId={courseId}/>;
}
export default GroupOverview