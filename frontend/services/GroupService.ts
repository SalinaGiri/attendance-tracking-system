import { Group } from "@types";

const apiUrl = process.env.NEXT_PUBLIC_API_URL;

const findGroupsByCourseId = async (courseId: number): Promise<Group[]> => {
    const response = await fetch(apiUrl + "/groups/" + courseId, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
        },
    })

    return response.json()
}

const GroupService = {
    findGroupsByCourseId
};

export default GroupService;