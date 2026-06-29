"use client";
import { useRouter } from "next/navigation";
import { StatusMessage, Student } from "@types";
import { useEffect, useState } from "react";
import AddStudentForm from "@components/groups/addStudentForm";
import UploadStudents from "./uploadStudents";
import { removeStudentFromGroupAction } from "actions/courseActions";
import GoBack from "@components/goBack";

interface Props {
    courseId: number;
    groupName: string;
    initialStudents: Student[];
}

const RegisteredStudentTable = ({ courseId, groupName, initialStudents }: Props) => {
    const [students, setStudents] = useState<Student[]>(initialStudents);
    const [search, setSearch] = useState("");
    const [sortBy, setSortBy] = useState<"studentNumber" | "name">("studentNumber");
    const [sortAsc, setSortAsc] = useState(true);
    const [feedback, setFeedback] = useState<StatusMessage | null>(null);
    const [showAddForm, setShowAddForm] = useState(false);
    const [highlightedStudents, setHighlightedStudents] = useState<string[]>([]);

    const router = useRouter();

    useEffect(() => {
        if (feedback) {
            const timer = setTimeout(() => setFeedback(null), 6000);
            return () => clearTimeout(timer);
        }
    }, [feedback]);

    const handleRemove = async (studentNumber: string) => {

        const student = students.find(s => s.studentNumber === studentNumber);
        if (!student) return;

        const fullName = `${student.firstName} ${student.lastName}`;
        if (!confirm(`Remove ${fullName} from the group?`)) return;

        try {
            await removeStudentFromGroupAction(courseId, groupName, studentNumber);
            setStudents(students.filter(s => s.studentNumber !== studentNumber));
            setFeedback({ message: "Student removed successfully", type: "success" });
        } catch (err: any) {
            setFeedback({ message: err.message || "Failed to remove student", type: "error" });
        }
    };

    const handleAddStudents = (newStudents: Student[]) => {
        let addedCount = 0;
        let duplicateCount = 0;
        const newlyAdded: string[] = [];

        newStudents.forEach(s => {
            const exists = students.find(st => st.studentNumber === s.studentNumber);

            if (!exists) {
                setStudents(prev => [...prev, s]);
                addedCount++;
                newlyAdded.push(s.studentNumber);
            } else {
                duplicateCount++;
            }
        });

        if (newlyAdded.length > 0) {
            setHighlightedStudents(prev => [...prev, ...newlyAdded]);
            setTimeout(() => {
                setHighlightedStudents(prev => prev.filter(sn => !newlyAdded.includes(sn)));
            }, 1000);
        }

        if (addedCount && duplicateCount) {
            setFeedback({
                message: `${addedCount} student(s) added, ${duplicateCount} duplicate(s) skipped.`,
                type: "success"
            });
        } else if (addedCount) {
            setFeedback({
                message: `${addedCount} student(s) added successfully.`,
                type: "success"
            });
        } else if (duplicateCount) {
            setFeedback({
                message: `${duplicateCount} student(s) already exist — no new students added.`,
                type: "success"
            });
        }
    };

    const sortedAndFiltered = students
        .filter(s =>
            (s.studentNumber?.toLowerCase().includes(search.toLowerCase()) ?? false) ||
            ((s.lastName).toLowerCase().includes(search.toLowerCase()))
        )
        .sort((a, b) => {
            if (sortBy === "studentNumber") {
                const aNum = a.studentNumber ?? "";
                const bNum = b.studentNumber ?? "";
                return sortAsc ? aNum.localeCompare(bNum) : bNum.localeCompare(aNum);
            } else if (sortBy === "name") {
                const aName = `${a.lastName}`
                const bName = `${b.lastName}`;
                return sortAsc ? aName.localeCompare(bName) : bName.localeCompare(aName);
            }
            return 0;
        });

    return <>
        <div>
            {feedback && (
                <div
                    className={`px-4 py-2 rounded mb-2 text-center max-w-md mx-auto 
                        ${feedback.type === "success" ? "bg-green-600 text-white" : "bg-red-600 text-white"
                        }`}
                >
                    {feedback.message}
                </div>
            )}



            <div className="mb-2">
                <div className="flex space-x-2 mb-2">
                    <GoBack url={`/courses/${courseId}`}/>
                    <button
                        className="bg-cats-dark-green text-white rounded px-3 py-1 hover:bg-black transition-colors"
                        onClick={() => setShowAddForm(prev => !prev)}
                    >
                        {showAddForm ? "Close Form" : "Add Student Manually"}
                    </button>

                    <UploadStudents
                        courseId={courseId}
                        groupName={groupName}
                        onSuccess={(newStudents) => handleAddStudents(newStudents)}
                        onError={(msg) => setFeedback({ message: msg, type: "error" })}
                    />
                </div>

                <input
                    type="text"
                    placeholder="Search by student number or name"
                    value={search}
                    onChange={e => setSearch(e.target.value)}
                    className="border rounded px-2 py-1 w-full"
                />
            </div>


            {showAddForm && (
                <AddStudentForm
                    courseId={courseId}
                    groupName={groupName}
                    onAdd={(student: Student) => handleAddStudents([student])}
                    onError={(msg: string) => setFeedback({ message: msg, type: "error" })}
                />
            )}


            <table className="w-full border rounded mt-2 text-left min-w-[600px]">
                <thead>
                    <tr className="bg-cats-light-green">
                        <th
                            className="p-2 cursor-pointer"
                            onClick={() => { setSortBy("studentNumber"); setSortAsc(sortBy === "studentNumber" ? !sortAsc : true); }}
                        >
                            Student Number
                        </th>
                        <th
                            className="p-2 cursor-pointer"
                            onClick={() => { setSortBy("name"); setSortAsc(sortBy === "name" ? !sortAsc : true); }}
                        >
                            Name
                        </th>
                        <th
                            className="p-2 cursor-pointer"
                        >
                            FirstName
                        </th>
                        <th className="p-2">Actions</th>
                    </tr>
                </thead>
                <tbody>
                    {sortedAndFiltered.map(s => (
                        <tr key={s.studentNumber} className={
                            `border-t transition-colors ${highlightedStudents.includes(s.studentNumber) ? "bg-orange-300" : ""}`
                        }>
                            <td className="p-2">{s.studentNumber}</td>
                            <td className="p-2">{s.lastName}</td>
                            <td className="p-2">{s.firstName}</td>
                            <td className="p-2">
                                <button
                                    onClick={() => handleRemove(s.studentNumber)}
                                    className="bg-red-600 text-white rounded px-2 py-1 hover:bg-red-700 transition-colors"
                                >
                                    Remove
                                </button>
                            </td>
                        </tr>
                    ))}
                    {sortedAndFiltered.length === 0 && (
                        <tr>
                            <td colSpan={3} className="p-2 text-center text-gray-500">
                                No students found
                            </td>
                        </tr>
                    )}
                </tbody>
            </table>
        </div>
    </>
};

export default RegisteredStudentTable;