"use client";
import { GroupWithCount } from "@types";
import { useState } from "react";
import { uploadGroupAction, deleteGroupAction, getGroupWithStudentCountAction } from "actions/courseActions";


type Props = {
    courseId: number;
    groups: GroupWithCount[];
    setGroups: (groups: GroupWithCount[]) => void;
    highlightGroupId: number | null;
};

const GroupTable = ({ courseId, groups, setGroups, highlightGroupId }: Props) => {
    const [uploading, setUploading] = useState<string | null>(null);
    const [uploadError, setUploadError] = useState<string | null>(null);
    const [warningMessage, setWarningMessage] = useState<string | null>(null);
    const [uploadProgress, setUploadProgress] = useState<string | null>(null);

    const displayNonStandardStudentNumbers = (studentNumbers: Array<String>) => {
        setWarningMessage(`A certain number of students (${studentNumbers.length})
             have a non-standard number. Their IDs: `
             + studentNumbers.join(", "));
    }

    const handleFileUpload = async (groupName: string, files: FileList) => {
        setUploading(groupName);
        setUploadError(null);
        setWarningMessage(null);
        setUploadProgress(null);

        // Convert FileList to array
        const fileArray = Array.from(files);
        
        try {
            const nonStandardStudentNumbers: string[] = [];
            const results: { file: string; success: boolean }[] = [];

            // Upload files sequentially
            for (let i = 0; i < fileArray.length; i++) {
                const file = fileArray[i];
                setUploadProgress(`Uploading file ${i + 1} of ${fileArray.length}: ${file.name}`);

                try {
                    const data = await uploadGroupAction(courseId, groupName, file);
                    if (data.nonStandardStudentNumbers.length > 0) {
                        nonStandardStudentNumbers.push(...data.nonStandardStudentNumbers);
                    }
                    results.push({ file: file.name, success: true });
                } catch (err) {
                    results.push({ file: file.name, success: false });
                    const message =
                        err?.response?.data?.error ||
                        err?.message ||
                        "Unknown error occurred";
                    
                    // For multiple files, collect errors instead of showing immediately
                    if (fileArray.length === 1) {
                        throw err; // Single file - throw to be caught by outer catch
                    }
                }
            }

            // Show unprocessed students if any
            if (nonStandardStudentNumbers.length > 0) {
                displayNonStandardStudentNumbers(nonStandardStudentNumbers);
            }

            // Check results
            const failedFiles = results.filter(r => !r.success);
            if (failedFiles.length > 0 && fileArray.length > 1) {
                setUploadError(`${failedFiles.length} file(s) failed to upload: ${failedFiles.map(f => f.file).join(', ')}`);
                setTimeout(() => setUploadError(null), 5000);
            }

            // Update student count
            const updatedGroup = await getGroupWithStudentCountAction(courseId, groupName);
            setGroups(groups.map((g) =>
                g.name === groupName ? { ...g, studentCount: updatedGroup.studentCount } : g
            ));
        } catch (err) {
            const message =
                err?.response?.data?.error ||
                err?.message ||
                "Unknown error occurred";

            setUploadError(message);
            setTimeout(() => setUploadError(null), 3000);
        } finally {
            setUploading(null);
            setUploadProgress(null);
        }
    };

    const handleDeleteGroup = async (groupName: string) => {
        if (!confirm(`Are you sure you want to delete ${groupName}?`)) return;
        try {
            await deleteGroupAction(courseId, groupName);
            setGroups(groups.filter((g) => g.name !== groupName));
        } catch (err) {
            console.error(err);
        }
    };

    return <>
        {uploadError &&
            <p className="bg-red-600 text-white rounded-md text-sm mb-2 text-center">
                {uploadError}
            </p>
        }
        {uploadProgress && (
            <div className="bg-blue-100 text-blue-800 rounded-md text-sm mb-2 text-center p-2">
                {uploadProgress}
            </div>
        )}
        {!uploadError && warningMessage &&
            <div className="bg-amber-600 text-white rounded-xl px-3 py-1 flex flex-col items-center">
                <p>
                    {warningMessage}
                </p>

                <button 
                onClick={() => setWarningMessage(null)}
                className="bg-cats-dark-green text-white rounded-xl px-3 py-1 hover:bg-black transition-colors"
                >Close</button>
            </div>
        }
        <table className="w-full bg-cats-medium-white rounded-xl text-center min-w-[600px]">
            <thead>
                <tr>
                    <th className="p-2 text-center">Group</th>
                    <th className="p-2 text-center">Students</th>
                    <th className="p-2 text-center">Student Count</th>
                    <th className="p-2 text-center">Delete</th>
                </tr>
            </thead>
            <tbody>
                {groups.map((group) => {
                    const hasStudents = group.studentCount > 0;
                    return (
                        <tr key={group.id} className={`border-t transition-colors duration-500 
                            ${highlightGroupId === group.id ? "bg-orange-300" : ""}`}>
                            <td className="p-2">{group.name}</td>
                            <td className="p-2 flex justify-center">
                                {hasStudents ? (
                                    <a
                                        href={`/courses/${courseId}/groups/${group.name}`}
                                        className="bg-cats-dark-green text-white rounded-xl px-4 py-1 hover:bg-black transition-colors"
                                    >
                                        View Students
                                    </a>
                                ) : (
                                    <label className="bg-cats-dark-green text-white rounded-xl px-4 py-1 cursor-pointer hover:bg-black transition-colors">
                                        {uploading === group.name ? "Uploading..." : "Upload File(s)"}
                                        <input
                                            type="file"
                                            accept=".xlsx,.xls"
                                            multiple
                                            className="hidden"
                                            onChange={(e) =>
                                                e.target.files && e.target.files.length > 0 &&
                                                handleFileUpload(group.name, e.target.files)
                                            }
                                        />
                                    </label>
                                )}
                            </td>
                            <td className="p-2">{group.studentCount}</td>
                            <td className="p-2">
                                <button
                                    onClick={() => handleDeleteGroup(group.name)}
                                    className="bg-red-600 text-white rounded-xl px-4 py-1 hover:bg-red-700 transition-colors"
                                >
                                    X
                                </button>
                            </td>
                        </tr>
                    );
                })}

                {groups.length === 0 && (
                    <tr>
                        <td colSpan={4} className="p-1 text-center text-gray-500">
                            No groups found
                        </td>
                    </tr>
                )}
            </tbody>
        </table>
    </>

};

export default GroupTable;