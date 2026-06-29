"use client";

import { Student } from "@types";
import { addStudentToGroupManuallyAction } from "actions/courseActions";
import { useState } from "react";

type Props = {
    courseId: number;
    groupName: string;
    onAdd: (student: Student) => void;
    onError: (message: string) => void;
};

const AddStudentForm = ({ courseId, groupName, onAdd, onError }: Props) => {
    const [studentNumber, setStudentNumber] = useState("");
    const [firstName, setFirstName] = useState("");
    const [lastName, setLastName] = useState("");
    const [submitting, setSubmitting] = useState(false);

    const validateStudentNumber = (r: string) => /^\w\d{7}$/i.test(r);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        if (!studentNumber || !firstName || !lastName) {
            onError("All fields are required.");
            return;
        }

        if (!validateStudentNumber(studentNumber)) {
            onError("Student number must start with a letter followed by 7 digits.");
            return;
        }

        setSubmitting(true);

        try {
            const newStudent: Student = { studentNumber, firstName, lastName };

            await addStudentToGroupManuallyAction(courseId, groupName, newStudent);

            onAdd(newStudent);

            setStudentNumber("");
            setFirstName("");
            setLastName("");
        } catch (err: any) {
            onError(err.message || "Failed to add student.");
        } finally {
            setSubmitting(false);
        }
    };

    return <>
        <form onSubmit={handleSubmit} className="border p-4 rounded mb-2 max-w-md mx-auto bg-cats-medium-white">
            <div className="mb-2">
                <label className="block mb-1 font-semibold">Student Number</label>
                <input
                    type="text"
                    value={studentNumber}
                    onChange={(e) => setStudentNumber(e.target.value)}
                    className="border rounded px-2 py-1 w-full"
                    placeholder="r1234567"
                />
            </div>

            <div className="mb-2">
                <label className="block mb-1 font-semibold">First Name</label>
                <input
                    type="text"
                    value={firstName}
                    onChange={(e) => setFirstName(e.target.value)}
                    className="border rounded px-2 py-1 w-full"
                    placeholder="John"
                />
            </div>

            <div className="mb-2">
                <label className="block mb-1 font-semibold">Last Name</label>
                <input
                    type="text"
                    value={lastName}
                    onChange={(e) => setLastName(e.target.value)}
                    className="border rounded px-2 py-1 w-full"
                    placeholder="UCLL"
                />
            </div>

            <button
                type="submit"
                disabled={submitting}
                className="bg-cats-dark-green text-white px-4 py-1 rounded hover:bg-black transition-colors"
            >
                {submitting ? "Adding..." : "Add Student"}
            </button>
        </form>
    </>;
};
export default AddStudentForm;