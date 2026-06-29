import { Course, Student } from "@types";

const apiUrl = process.env.NEXT_PUBLIC_API_URL;

const getAll = async (): Promise<Course[]> => {
    const response = await fetch(apiUrl + '/courses', {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
        },
    })

    return response.json()
}

const addCourse = async (name: string, description: string) => {
    const course: Course = { name, description }
    const response = await fetch(apiUrl + '/courses', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(course)
    })

    return response.json()
}

const findCourseById = async (courseId: number) => {
    const response = await fetch(apiUrl + `/courses/${courseId}`, {
        method: "GET",
        headers: {
            "Content-Type": "application/json"
        }
    })
    return response.json()
}

const findEventsByCourseId = async (courseId: number) => {
    const response = await fetch(apiUrl + "/courses/" + courseId + "/events", {
        method: "GET",
        headers: {
            "Content-Type": "application/json"
        },
    })

    return response.json()
}

const addCourseWithFile = async (name: string, description: string, file: File) => {
    const formData = new FormData();
    formData.append('name', name);
    formData.append('description', description);
    formData.append('file', file);

    return await fetch(`${apiUrl}/courses`, {
        method: 'POST',
        body: formData
    });
};

const getGroupsByCourseId = async (courseId: number) => {
    const response = await fetch(`${apiUrl}/courses/${courseId}/groups`, {
        method: 'GET',
        headers: { 'Content-Type': 'application/json' },
    });
    return response.json();
}

const getStudentsByGroup = async (courseId: number, groupName: string) => {
    const response = await fetch(`${apiUrl}/courses/${courseId}/groups/${groupName}`, {
        method: 'GET',
        headers: { 'Content-Type': 'application/json' },
    });
    return response.json();
};

const createGroup = async (courseId: number, groupName: string) => {
    const response = await fetch(`${apiUrl}/courses/${courseId}/groups`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: groupName,
    });
    return response.json();
};

const uploadStudentsToGroup = async (courseId: number, groupName: string, file: File) => {
    const formData = new FormData();
    formData.append('file', file);
    const response = await fetch(`${apiUrl}/courses/${courseId}/groups/${encodeURIComponent(groupName)}/students`, {
        method: 'POST',
        body: formData,
    });
    const data = await response.json();

    if (!response.ok) {
        throw new Error(data.error || "Unknown error occurred");
    }
    return data
};

const deleteGroup = async (courseId: number, groupName: string) => {
    const response = await fetch(`${apiUrl}/courses/${courseId}/groups/${encodeURIComponent(groupName)}`,
        {
            method: "DELETE",
            headers: { "Content-Type": "application/json" },
        });
    return response.text();
}

const getGroupWithStudentCount = async (courseId: number, groupName: string) => {
    const response = await fetch(`${apiUrl}/courses/${courseId}/groups/${encodeURIComponent(groupName)}/count`,
        {
            method: "GET",
            headers: { "Content-Type": "application/json" },
        }
    );
    return response.json()
}

const removeStudentFromGroup = async (
    courseId: number,
    groupName: string,
    studentNumber: string
) => {
    const response = await fetch(
        `${apiUrl}/courses/${courseId}/groups/${encodeURIComponent(groupName)}/students/${studentNumber}`,
        {
            method: "DELETE",
            headers: { "Content-Type": "application/json" },
        }
    );

    const data = await response.json();
    if (!response.ok) {
        throw new Error(data.error || "Failed to remove student");
    }

    return data;
};

const addStudentToGroupManually = async (
    courseId: number,
    groupName: string,
    newStudent: Student
) => {
    const response = await fetch(
        `${apiUrl}/courses/${courseId}/groups/${encodeURIComponent(groupName)}/students/manual`,
        {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(newStudent),
        }
    );

    const data = await response.json();
    if (!response.ok) {
        throw new Error(data.error || "Failed to add student manually");
    }

    return data;
};

const CourseService = {
    getAll,
    addCourse,
    addCourseWithFile,
    findEventsByCourseId,
    findCourseById,
    getGroupsByCourseId,
    getStudentsByGroup,
    createGroup,
    uploadStudentsToGroup,
    deleteGroup,
    getGroupWithStudentCount,
    addStudentToGroupManually,
    removeStudentFromGroup,
};

export default CourseService
