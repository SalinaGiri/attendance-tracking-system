import StatisticsOverview from "@components/statistics/StatisticsOverview";
import { getAllByCourseId } from "actions/registrationActions";

type Props = {
    courseId: string | null;
}

const getAllRegistrationsByCourseId = async ({courseId}: Props) => {
    return await getAllByCourseId(courseId);
}

const StatisticsPage = async ({ params }: {params: Promise<{id: string}>}) => {
    const {id : idString} = await params;

    const registrations = await getAllRegistrationsByCourseId({courseId: idString});

    return (
        <>
            <StatisticsOverview registrations={registrations}/>
        </>
    )


}

export default StatisticsPage;