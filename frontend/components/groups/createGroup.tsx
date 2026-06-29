"use client";

import CourseService from "@services/CourseService";
import { GroupWithCount } from "@types";
import { useState } from "react";
import { createGroupAction } from "actions/courseActions";
import GoBack from "@components/goBack";

type Props = {
    courseId: number;
    groups: GroupWithCount[];
    setGroups: (groups: GroupWithCount[]) => void;
    setHighlightGroupId: (id: number | null) => void;
};

const CreateGroup = ({ courseId, groups, setGroups, setHighlightGroupId }: Props) => {
    const [newGroupName, setNewGroupName] = useState("");
    const [error, setError] = useState("");

    const triggerError = (msg: string) => {
        setError(msg);
        setTimeout(() => setError(""), 3000);
    };


    const handleCreateGroup = async () => {
        if (!newGroupName.trim()) {
            triggerError("Group name cannot be empty.");
            return;
        }

        const existing = groups.find(
            (g) => g.name.toLowerCase() === newGroupName.toLowerCase()
        );
        if (existing) {
            setHighlightGroupId(existing.id);
            setTimeout(() => setHighlightGroupId(null), 1000);
            setNewGroupName("");
            setError("");
            return;
        }

        try {
            const newGroup = await createGroupAction(courseId, newGroupName);
            setGroups([...groups, { ...newGroup, studentCount: 0 }]);
            setNewGroupName("");
            setHighlightGroupId(newGroup.id);
            setError("");
            setTimeout(() => setHighlightGroupId(null), 1000);
        } catch (err) {
            triggerError("Failed to create group.");
        }
    };

    return <>

        <div className="flex justify-between mb-4">
            <div className="flex space-x-2">
                <input
                    type="text"
                    value={newGroupName}
                    onChange={(e) => { setNewGroupName(e.target.value); }}
                    placeholder="New group name"
                    className="rounded-xl px-2 w-3/4"
                />
                <button
                    onClick={handleCreateGroup}
                    className="bg-cats-dark-green text-white rounded-xl px-3 hover:bg-black transition-colors"
                >
                    Create
                </button>
            </div>
            <div className="ml-auto">
                <GoBack url="/" isRounded />
            </div>

        </div>
        {error && (
            <p className="bg-red-600 text-white text-sm px-3 py-2 rounded-lg mb-2 w-full">
                {error}
            </p>
        )}
    </>
};

export default CreateGroup;