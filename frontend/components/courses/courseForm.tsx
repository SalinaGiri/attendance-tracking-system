'use client'

import { useState } from "react";
import { useRouter } from "next/navigation";
import { createCourseAction } from "actions/courseActions";
import GoBack from "@components/goBack";

type Props = {
    courseName: string;
    courseDescription: string;
}

const CourseForm: React.FC<Props> = ({ courseName, courseDescription }: Props) => {
    const [name, setName] = useState("");
    const [description, setDescription] = useState("");
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [status, setStatus] = useState<{ type: "success" | "error", message: string } | null>(null);

    const router = useRouter()

    const handlePost = async (e: React.FormEvent) => {
        e.preventDefault();

        if (!name || !description) {
            setStatus({ type: 'error', message: 'Course name and description are required.' });
            return;
        }

        setIsSubmitting(true);
        setStatus(null);

        try {
            const response = await createCourseAction(name, description);
            setStatus({ type: 'success', message: 'Course created successfully! Redirecting...' });
            setTimeout(() => router.push('/'), 2000);
        } catch {
            setStatus({ type: 'error', message: 'Network error during course creation.' });
        } finally {
            setIsSubmitting(false);
        }
    };

    return <>
        <form onSubmit={handlePost} className="border rounded-2xl p-6 bg-cats-medium-white max-w-md mx-auto shadow-sm space-y-4">

            <div className="mb-2">
                <label className="block mb-1 font-semibold">Course Name</label>
                <input
                    type="text"
                    value={name}
                    onChange={e => setName(e.target.value)}
                    className="border rounded px-2 py-1 w-full"
                    placeholder="Enter course name"
                />
            </div>

            <div className="mb-2">
                <label className="block mb-1 font-semibold">Description</label>
                <input
                    type="text"
                    value={description}
                    onChange={e => setDescription(e.target.value)}
                    className="border rounded px-2 py-1 w-full"
                    placeholder="Enter course description"
                />
            </div>

            {status && (
                <div className={`p-3 rounded-md text-sm ${status.type === 'success' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}`}>
                    {status.message}
                </div>
            )}

            <div className="flex justify-center">
                <button
                    type="submit"
                    disabled={isSubmitting}
                    className="bg-cats-dark-green text-white rounded-xl px-3 py-1 hover:bg-black transition-colors"
                >
                    {isSubmitting ? 'Creating...' : 'Create Course'}
                </button>
                <div className="ml-auto">
                    <GoBack url="/" isRounded />
                </div>

            </div>
        </form>
    </>;
};

export default CourseForm
