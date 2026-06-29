"use client";

import CourseService from "@services/CourseService";
import { Student } from "@types";
import { uploadGroupAction } from "actions/courseActions";
import { useState } from "react";

type Props = {
    courseId: number;
    groupName: string;
    onSuccess: (students: Student[]) => void;
    onError: (message: string) => void;
}

const UploadStudents = ({ courseId, groupName, onSuccess, onError }: Props) => {
    const [uploading, setUploading] = useState(false);

    const handleFileChange = async (file: File) => {
        setUploading(true);
        try {
            const formData = new FormData();
            formData.append("file", file);

            const response = await uploadGroupAction(courseId, groupName, file);

            const newStudents: Student[] = response.students;
            if (!newStudents || newStudents.length === 0) {
                onError("No valid students found in the uploaded file.");
            } else {
                onSuccess(newStudents);
            }
        } catch (err: any) {
            onError(err?.message || "Failed to upload students");
        } finally {
            setUploading(false);
        }
    };

    return (
        <label className="bg-cats-dark-green text-white rounded px-3 py-1 cursor-pointer hover:bg-black transition-colors">
            {uploading ? "Uploading..." : "Upload Student File"}
            <input
                type="file"
                accept=".xlsx,.xls"
                className="hidden"
                onChange={(e) => e.target.files?.[0] && handleFileChange(e.target.files[0])}
            />
        </label>
    );
}

export default UploadStudents;