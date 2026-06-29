"use client";
import { useState } from "react";
import CreateGroup from "@components/groups/createGroup";
import GroupTable from "@components/groups/groupTable";
import { GroupWithCount } from "@types";

type Props = {
    initialGroups: GroupWithCount[];
    courseId: number;
};

const GroupOverviewClient = ({ initialGroups, courseId }: Props) => {
    const [groups, setGroups] = useState<GroupWithCount[]>(initialGroups);
    const [highlightGroupId, setHighlightGroupId] = useState<number | null>(null);

    return (
        <div className="p-4 bg-cats-light-green rounded-xl space-y-6">
            <CreateGroup
                courseId={courseId}
                groups={groups}
                setGroups={setGroups}
                setHighlightGroupId={setHighlightGroupId}
            />
            <GroupTable
                courseId={courseId}
                groups={groups}
                setGroups={setGroups}
                highlightGroupId={highlightGroupId}
            />
        </div>
    );
};

export default GroupOverviewClient;