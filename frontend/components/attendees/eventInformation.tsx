'use client'

import { Event } from "@types";
import { fetchEventByEventIdAction } from "actions/eventActions";
import { useParams } from "next/navigation";
import { useEffect, useState } from "react";

const EventInformation: React.FC = () => {
    const [event, setEvent] = useState<Event>(null);
    const [isLoading, setIsLoading] = useState(false);

    const params = useParams<{ eventId: string; }>();
    const eventId = Number(params.eventId);

    const fetchEventById = async (eventId: number) => {
        try {
            setIsLoading(true);
            const fetchedEvent = await fetchEventByEventIdAction(
                eventId
            );
            setEvent(fetchedEvent);
        } catch (error) {
            console.error("Error fetching event: ", error);
            setEvent(null);
        } finally {
            setIsLoading(false)
        }
    }

    useEffect(() => {
        fetchEventById(eventId);
    }, [eventId]);

    return (
        <>
            <section className="flex justify-center gap-10">
                <table className="bg-cats-medium-white mt-5 rounded-xl mx-auto overflow-hidden">
                    <tbody>
                        {isLoading && (
                            <tr>
                                <td>Loading...</td>
                            </tr>
                        )}
                        {!event && !isLoading && (
                            <tr>
                                <td>Failed to fetch event information</td>
                            </tr>
                        )}
                        {event && !isLoading && (<>
                            <tr>
                                <th className="bg-cats-dark-white">Event Name</th>
                                <td className="bg-white">{event.eventName}</td>
                            </tr>
                            {event.checkInTime &&(
                                <tr className="bg-cats-dark-white">
                                    <th className="text-left">Check-in Time</th>
                                    <td className="bg-white">
                                        {new Date(event.checkInTime).toLocaleDateString([], {
                                            hour: "2-digit",
                                            minute: "2-digit"
                                        })}
                                    </td>
                                </tr>
                            )}
                            {event.checkOutTime && (
                                <tr className="bg-cats-dark-white">
                                    <th className="text-left">Check-out Time</th>
                                    <td className="bg-white">
                                        {new Date(event.checkOutTime).toLocaleDateString([], {
                                            hour: "2-digit",
                                            minute: "2-digit"
                                        })}
                                    </td>
                                </tr>
                            )}
                            <tr className="bg-cats-dark-white">
                                <th>Groups</th>
                                <td className="bg-white">
                                    {event.groups.map(group => group.name).join(", ")}
                                </td>
                            </tr>
                        </>)}
                    </tbody>
                </table>
            </section>
        </>
    )
}

export default EventInformation;