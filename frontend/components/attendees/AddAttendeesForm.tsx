"use client";

import { useState } from "react";
import { AttendeeV2 } from "@types";
import { addRegistrationManualAction } from "actions/registrationActions";

type Props = {
    eventId: number;
    onAdd: (attendee: AttendeeV2) => void;
    onError: (message: string) => void;
};

export default function AddAttendeesForm({ eventId, onAdd, onError }: Props) {
    const [studentNumber, setStudentNumber] = useState("");
    const [firstName, setFirstName] = useState("");
    const [lastName, setLastName] = useState("");
    const [checkIn, setCheckIn] = useState("");
    const [checkOut, setCheckOut] = useState("");
    const [loading, setLoading] = useState(false);

    const validateStudentNumber = (r: string) => /^\w\d{7}$/i.test(r);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        if (!studentNumber || !firstName || !lastName) {
            onError("All fields except times are required.");
            return;
        }

        if (!validateStudentNumber(studentNumber)) {
            onError("Student number must start with a letter and have 7 digits, e.g. r1234567.");
            return;
        }

        if (!checkIn && !checkOut) {
            onError("You must provide at least check-in or check-out time.");
            return;
        }

        const payload = {
            eventId:eventId,
            studentNumber:studentNumber,
            firstName:firstName,
            lastName:lastName,
            checkInTime: checkIn ? checkIn + ":00" : null,
            checkOutTime: checkOut ? checkOut + ":00" : null,
        };

        try {
            setLoading(true);

            // Call your service method directly
            const created = await addRegistrationManualAction(eventId, payload);

            onAdd(created);

            setStudentNumber("");
            setFirstName("");
            setLastName("");
            setCheckIn("");
            setCheckOut("");

        } catch (err: any) {
            onError(err.message || "Failed to add attendee.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <form onSubmit={handleSubmit} className="border p-4 rounded bg-cats-medium-white max-w-md mx-auto">
            <h2 className="font-bold mb-3 text-lg">Add Attendee</h2>

            <div className="mb-2">
                <label className="block mb-1">Student Number</label>
                <input
                    value={studentNumber}
                    onChange={(e) => setStudentNumber(e.target.value)}
                    placeholder="r1021440"
                    className="border rounded px-2 py-1 w-full"
                />
            </div>

            <div className="mb-2">
                <label className="block mb-1">First Name</label>
                <input
                    value={firstName}
                    onChange={(e) => setFirstName(e.target.value)}
                    placeholder="Salina"
                    className="border rounded px-2 py-1 w-full"
                />
            </div>

            <div className="mb-2">
                <label className="block mb-1">Last Name</label>
                <input
                    value={lastName}
                    onChange={(e) => setLastName(e.target.value)}
                    placeholder="Giri"
                    className="border rounded px-2 py-1 w-full"
                />
            </div>

            <div className="mb-2">
                <label className="block mb-1">Check-in Time (optional)</label>
                <input
                    type="datetime-local"
                    value={checkIn}
                    onChange={(e) => setCheckIn(e.target.value)}
                    className="border rounded px-2 py-1 w-full"
                />
            </div>

            <div className="mb-2">
                <label className="block mb-1">Check-out Time (optional)</label>
                <input
                    type="datetime-local"
                    value={checkOut}
                    onChange={(e) => setCheckOut(e.target.value)}
                    className="border rounded px-2 py-1 w-full"
                />
            </div>

            <button
                type="submit"
                disabled={loading}
                className="bg-cats-dark-green text-white px-4 py-1 rounded hover:bg-black transition-colors mt-2"
            >
                {loading ? "Adding..." : "Add Attendee"}
            </button>
        </form>
    );
}
