"use client"

import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { Group } from "@types";
import type { EventInput } from "@types";
import { createEventAction } from "actions/eventActions";
import { fetchGroupsByCourseIdAction } from "actions/groupActions";
import { fetchCourseByCourseIdAction } from "actions/courseActions";
import EventDateTimeForm from "@components/events/EventDateTimeForm";
import GoBack from "@components/goBack";

type Props = {
  courseId: number;
}

type CheckType = "checkin" | "checkout" | "both";

const validateEvent = (
  eventName: string,
  checkType: CheckType,
  checkInTime: string | null,
  checkOutTime: string | null
): string | null => {
  if (!eventName.trim()) {
    return "Event name is required.";
  }

  if (checkType === "checkin" && !checkInTime) {
    return "Check-in time is required.";
  }

  if (checkType === "checkout" && !checkOutTime) {
    return "Check-out time is required.";
  }

  if (checkType === "both") {
    if (!checkInTime || !checkOutTime) {
      return "Both check-in and check-out times are required.";
    }

    const checkIn = new Date(checkInTime);
    const checkOut = new Date(checkOutTime);

    if (checkIn >= checkOut) {
      return "Check-out time must be after check-in time.";
    }
  }

  return null;
};

export const AddEventOverview: React.FC<Props> = ({ courseId }: Props) => {
  const router = useRouter();
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [allGroups, setAllGroups] = useState<Group[]>([]);
  const [selectedGroups, setSelectedGroups] = useState<Group[]>([]);

  const [checkType, setCheckType] = useState<CheckType>("checkin");
  const [checkInTime, setCheckInTime] = useState<string | null>(null);
  const [checkOutTime, setCheckOutTime] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const isAllSelected =
    allGroups.length > 0 && selectedGroups.length === allGroups.length;

  useEffect(() => {
    const fetchGroups = async () => {
      try {
        const response = await fetchGroupsByCourseIdAction(courseId);
        setAllGroups(response);
      } catch (err) {
        console.error("Failed to load groups", err);
      }
    };

    fetchGroups();
  }, [courseId]);

  const handleGroupToggle = (group: Group, checked: boolean) => {
    setSelectedGroups(prev =>
      checked
        ? [...prev, group] // add if checked
        : prev.filter(g => g.id !== group.id) // remove if unchecked
    );
  };

  const handleSelectAll = (checked: boolean) => {
    setSelectedGroups(checked ? allGroups : []);
  };

  const toISOUCLL = (date: string) => {
    return date + ":00.000Z"
  }

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setErrorMessage(null);
    setSuccessMessage(null);

    const data = new FormData(e.currentTarget);
    const eventName = data.get("eventName") as string;

    const validationError = validateEvent(eventName, checkType, checkInTime, checkOutTime);

    if (validationError) {
      setErrorMessage(validationError);
      return;
    }

    const course = await (await fetchCourseByCourseIdAction(courseId));


    const payload: EventInput = {
      eventName: eventName,
      groups: selectedGroups
    };
    payload.checkInTime = checkInTime
      ? toISOUCLL(checkInTime)
      : null;
    payload.checkOutTime = checkOutTime
      ? toISOUCLL(checkOutTime)
      : null;
    payload.groups = (payload.groups.length !== 0) ? payload.groups : null;
    payload.course = course;

    setSubmitting(true);

    try {
      const res = await createEventAction(payload);
      setSuccessMessage("Event created successfully! Redirecting...");
      setTimeout(() => router.push(`/courses/${courseId}/events`), 2000);
    } catch (error) {
      setErrorMessage(error.message);
      return;
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="w-full bg-cats-medium-white border rounded p-4">
      {errorMessage && <p className="text-red-600 mb-2">{errorMessage}</p>}
      {successMessage && <p className="text-green-600 mb-2">{successMessage}</p>}

      <section>
        <form className="add-event-form min-w-100" onSubmit={handleSubmit} method="POST">
          <div>
            <label
              htmlFor="eventName"
              className="block mb-1 font-semibold"
            >Event name:</label>
            <input
              type="text"
              id="eventName"
              name="eventName"
              placeholder="Enter name here..."
              className="border rounded px-2 py-1 w-full"
            >
            </input>
          </div>

          <div className="mb-2">
            <label className="block mb-1 font-semibold">Event Type:</label>
            <select
              value={checkType}
              onChange={(e) => setCheckType(e.target.value as CheckType)}
              className="border rounded px-2 py-1 w-full"
            >
              <option value="checkin">Check-in</option>
              <option value="checkout">Check-out</option>
              <option value="both">Check-in & Check-out</option>
            </select>
          </div>

          {(checkType === "checkin" || checkType === "both") && (
            <EventDateTimeForm
              label="Check-in Date & Time"
              value={checkInTime}
              onChange={setCheckInTime}
            />
          )}

          {(checkType === "checkout" || checkType === "both") && (
            <EventDateTimeForm
              label="Check-out Date & Time"
              value={checkOutTime}
              onChange={setCheckOutTime}
            />
          )}

          <div className="mb-2 pt-4 pb-4">
            <label className="pb-1 block mb-1 font-semibold">Groups</label>
            <div className="flex items-center mb-2 pt-2">
              <label className="font-medium mr-2.5">All groups</label>
              <input
                type="checkbox"
                className="w-4 h-4"
                checked={isAllSelected}
                onChange={(e) => handleSelectAll(e.target.checked)}
              />
            </div>
            <hr className="my-2 border-t border-gray-300" />
            <div className="flex flex-wrap -mx-2">
              {allGroups && allGroups.map((group) => (
                <div className="w-1/2 px-2 mb-2 flex items-center" key={group.id}>
                  <label htmlFor={group.name}>{group.name}</label>
                  <input
                    type="checkbox"
                    id={group.name}
                    className="w-4 h-4 mr-2"
                    checked={selectedGroups.some(g => g.id === group.id)}
                    onChange={(e) => handleGroupToggle(group, e.target.checked)}
                  />
                </div>
              ))}
            </div>
          </div>
          <div className="mt-4 flex gap-2">
            <button
              type="submit"
              disabled={submitting}
              className="bg-cats-dark-green text-white px-4 py-1 rounded hover:bg-black transition-colors"
            >
              {submitting ? "Submitting..." : "Create Event"}
            </button>
            <GoBack url={`/courses/${courseId}/events`} />
          </div>
        </form>
      </section>
    </div>
  )
}

export default AddEventOverview;