'use client'

import EventInformation from "@components/attendees/eventInformation";
import AttendanceGauge from "@components/attendees/attendanceGauge";
import { useParams } from "next/navigation";
import { useEffect, useState } from "react";
import { AttendeeV2, Event, FilterCounts, Group } from "@types";
import { fetchEventByEventIdAction, fetchGroupsByEvent } from "actions/eventActions";
import { deleteRegistrationAction, getAllByEventId, toggleLegitimatelyAbsentAction } from "actions/registrationActions";
import GoBack from "@components/goBack";
import PathButton from "@components/urlButton";
import { calculateFallbackMode } from "next/dist/build/static-paths/app";
import RegistrationService from "@services/RegistrationService";
import AddAttendeesForm from "@components/attendees/AddAttendeesForm";


const Attendees = () => {
    useEffect(() => {
        document.title = "Attendee Overview"
    }, []);

    const [allStudents, setAllStudents] = useState<AttendeeV2[]>();
    const [students, setStudents] = useState<AttendeeV2[]>();
    const [event, setEvent] = useState<Event>(null);
    const [expectedStudents, setExpectedStudents] = useState<Set<string>>(new Set());
    const [filterCounters, setFilterCounters] = useState<FilterCounts>(null);
    const [isLoading, setIsLoading] = useState(false);
    const [toggledAbsence, setToggledAbsence] = useState(false);
    const [previousFilter, setPreviousFilter] = useState<null | string>(null);
    const [refreshFlag, setRefreshFlag] = useState(false);
    const [showAddForm, setShowAddForm] = useState(false);
    const [activeFilter, setActiveFilter] = useState<string>("all");
    const [studentGroupMap, setStudentGroupMap] = useState<Record<string, string[]>>({});


    const [search, setSearch] = useState("");


    const [sortField, setSortField] = useState<string | null>(null);
    const [sortOrder, setSortOrder] = useState<"asc" | "desc" | null>(null);

    const params = useParams<{ eventId: string; id: string }>();
    const eventId = Number(params.eventId);
    const courseId = Number(params.id)

    useEffect(() => {
        handleGetAllStudents(eventId)
    }, []);

    useEffect(() => {
        handleGetAllStudents(eventId)
    }, [toggledAbsence]);

    useEffect(() => {
        handleGetAllStudents(eventId)
    }, [refreshFlag]);

    useEffect(() => {
        if (allStudents) {
            applyActiveFilter();
        }
    }, [allStudents]);

    useEffect(() => {
        calculateFilterCounts()
    }, [allStudents]);

    useEffect(() => {
        calculateFilterCounts()
    }, [event]);

    useEffect(() => {
        fetchEventById(eventId);
    }, [eventId]);

    useEffect(() => {

        fetchEventGroups(eventId);


    }, [event])

    useEffect(() => {
        if (eventId) {
            fetchStudentGroups(eventId);
        }
    }, [eventId]);

    const fetchEventGroups = async (eventId: number) => {

        try {
            const fetchedGroups = await fetchGroupsByEvent(
                eventId
            );
            const newSet = new Set<string>();
            fetchedGroups.forEach(group => {
                group.assignedStudents.forEach(student => {
                    newSet.add(student.studentNumber)
                });
            });
            setExpectedStudents(newSet);
        } catch (error) {
            console.error("Error fetching Groups: ", error);
            setExpectedStudents(new Set())
        }

    }

    const fetchStudentGroups = async (eventId: number) => {
        try {
            const fetchedGroups = await fetchGroupsByEvent(eventId);

            const map: Record<string, string[]> = {};

            fetchedGroups.forEach(group => {
            group.assignedStudents.forEach(student => {
                    if (!map[student.studentNumber]) {
                    map[student.studentNumber] = [];    
                    }
                    map[student.studentNumber].push(group.name);
                });
            });
            setStudentGroupMap(map);
        } catch (err) {
            setStudentGroupMap({});
        }
    }

    const handleToggleLegitimatelyAbsent = async (attendee: AttendeeV2) => {
        if (event.eventType === 'CheckInOut') {
            // adding additional checks because an attendee may be HalfPresent
            if (!attendee.checkInId && !attendee.checkOutId) {
                await toggleLegitimatelyAbsentAction(attendee.id, true);
            }
            if (attendee.checkInId) {
                await toggleLegitimatelyAbsentAction(attendee.checkInId, false);
            } if (attendee.checkOutId) {
                await toggleLegitimatelyAbsentAction(attendee.checkOutId, false);
            }
        }
        if (event.eventType === 'CheckIn') {
            if (!attendee.checkInId) {
                await toggleLegitimatelyAbsentAction(attendee.id, true);
            } else {
                await toggleLegitimatelyAbsentAction(attendee.checkInId, false);
            }
        }
        if (event.eventType === 'CheckOut') {
            if (!attendee.checkOutId) {
                await toggleLegitimatelyAbsentAction(attendee.id, true);
            } else {
                await toggleLegitimatelyAbsentAction(attendee.checkOutId, false);
            }
        }

        setToggledAbsence(!toggledAbsence)
    };

    const calculateFilterCounts = async () => {
        const filterCounts: FilterCounts = {
            allCount: filterAllStudents(true),
            expectedCount: filterExpectedStudents(true),
            unexpectedPresentCount: filterUnexpectePresentStudents(true),
            presentCount: filterPresentStudents(true),
            absentCount: filterAbsentStudents(true),
            lateCheckInCount: filterLateCheckInStudents(true),
            lateCheckOutCount: filterLateCheckOutStudents(true),
            checkInCount: filterCheckInStudents(true),
            checkOutCount: filterCheckOutStudents(true)
        }
        setFilterCounters(filterCounts)
    }

    const handleGetAllStudents = async (eventId: number) => {
        const studentsResponse = await getAllByEventId(eventId);
        const studentsResult: any[] = await studentsResponse;
        const studentsResultWithTimedelta = studentsResult.map((s) => {
            s.timedeltaStr = timedelta(new Date(s.checkInTime), new Date(s.checkOutTime))
            return s
        })
        setAllStudents(studentsResultWithTimedelta);
    }

    const applyActiveFilter = () => {
        switch (activeFilter) {
            case "all":
                filterAllStudents();
                break;
            case "expected":
                filterExpectedStudents();
                break;
            case "present":
                filterPresentStudents();
                break;
            case "absent":
                filterAbsentStudents();
                break;
            case "unexpectedPresent":
                filterUnexpectePresentStudents();
                break;
            case "lateIn":
                filterLateCheckInStudents();
                break;
            case "lateOut":
                filterLateCheckOutStudents();
                break;
            case "checkInOnly":
                filterCheckInStudents();
                break;
            case "checkOutOnly":
                filterCheckOutStudents();
                break;
        }
    };

    const handleRemove = async (checkInId: number | null, checkOutId: number | null) => {
        if (!confirm("Are you sure you want to remove this attendee? This will delete both check-in and check-out records.")) return;

        try {

            if (checkInId) {
                await deleteRegistrationAction(checkInId);
            }

            if (checkOutId) {
                await deleteRegistrationAction(checkOutId);
            }

            setRefreshFlag(f => !f);

        } catch (err) {
            console.error("Delete failed", err);
            alert("Failed to delete registration. Please try again.");
        }
    };

    const filterExpectedStudents = (count: boolean = false) => {
        if (!allStudents) return 0;
        const filteredStudents: AttendeeV2[] = allStudents.filter((s) => {
            return s.status !== "Unexpected"
        });

        if (count) {
            return filteredStudents.length
        } else {
            setStudents(filteredStudents)
        }
    }

    const filterAllStudents = (count: boolean = false) => {
        if (!allStudents) return 0;
        if (count) {
            return allStudents.length
        } else {
            setStudents(allStudents)
        }
    }

    const filterCheckInStudents = (count: boolean = false) => {
        if (!allStudents || !event) return 0;
        const filteredStudents: AttendeeV2[] = allStudents.filter((s) => {
            const isAbsent = s.checkOutTime == null && s.checkInTime == null
            return !isAbsent && s.checkOutTime === null
        });

        if (count) {
            return filteredStudents.length
        } else {
            setStudents(filteredStudents)
        }
    }

    const filterCheckOutStudents = (count: boolean = false) => {
        if (!allStudents || !event) return 0;
        const filteredStudents: AttendeeV2[] = allStudents.filter((s) => {
            const isAbsent = s.checkOutTime == null && s.checkInTime == null
            return !isAbsent && s.checkInTime === null
        });

        if (count) {
            return filteredStudents.length
        } else {
            setStudents(filteredStudents)
        }
    }

    const filterAbsentStudents = (count: boolean = false) => {
        if (!allStudents || !event) return 0;
        const filteredStudents: AttendeeV2[] = allStudents.filter((s) => {
            return s.checkInTime === null && s.checkOutTime === null
        });

        if (count) {
            return filteredStudents.length
        } else {
            setStudents(filteredStudents)
        }
    }

    const filterPresentStudents = (count: boolean = false) => {
        if (!allStudents || !event) return 0;
        const filteredStudents: AttendeeV2[] = allStudents.filter((s) => {
            const studentCheckInDate = new Date(s.checkInTime);
            const eventCheckInDate = new Date(event.checkInTime);


            if (event.eventType == "CheckInOut") {
                const hasCheckInAndCheckOut = s.checkInTime !== null && s.checkOutTime !== null
                const checkedInTimely = studentCheckInDate < eventCheckInDate
                return hasCheckInAndCheckOut && checkedInTimely
            }

            if (event.eventType == "CheckIn") {
                const hasCheckInAndNoCheckOut = s.checkInTime !== null && s.checkOutTime == null
                const checkedInTimely = studentCheckInDate < eventCheckInDate
                return hasCheckInAndNoCheckOut && checkedInTimely
            }

            if (event.eventType == "CheckOut") {
                const hasNoCheckInAndCheckOut = s.checkInTime == null && s.checkOutTime !== null
                return hasNoCheckInAndCheckOut
            }

        });

        if (count) {
            return filteredStudents.length
        } else {
            setStudents(filteredStudents)
        }
    }

    const filterUnexpectePresentStudents = (count: boolean = false) => {
        if (!allStudents || !event) return 0;
        const filteredStudents: AttendeeV2[] = allStudents.filter((s) => {
            if (s.status !== "Unexpected") return false;

            const studentCheckInDate = new Date(s.checkInTime);
            const eventCheckInDate = new Date(event.checkInTime);

            if (event.eventType == "CheckInOut") {
                const hasCheckInAndCheckOut = s.checkInTime !== null && s.checkOutTime !== null
                const checkedInOnTime = studentCheckInDate < eventCheckInDate
                return hasCheckInAndCheckOut && checkedInOnTime
            }

            if (event.eventType == "CheckIn") {
                const hasCheckInAndNoCheckOut = s.checkInTime !== null && s.checkOutTime == null
                const checkedInOnTime = studentCheckInDate < eventCheckInDate
                return hasCheckInAndNoCheckOut && checkedInOnTime
            }

            if (event.eventType == "CheckOut") {
                const hasNoCheckInAndCheckOut = s.checkInTime == null && s.checkOutTime !== null
                return hasNoCheckInAndCheckOut
            }

            return false;
        });

        if (count) {
            return filteredStudents.length
        } else {
            setStudents(filteredStudents)
        }
    }

    const filterLateCheckInStudents = (count: boolean = false) => {
        if (!allStudents || !event) return 0;
        const filteredStudents: AttendeeV2[] = allStudents.filter((s) => {
            const studentCheckInDate = new Date(s.checkInTime);
            const eventCheckInDate = new Date(event.checkInTime);
            return studentCheckInDate > eventCheckInDate
        });

        if (count) {
            return filteredStudents.length
        } else {
            setStudents(filteredStudents)
        }
    }

    const filterLateCheckOutStudents = (count: boolean = false) => {
        if (!allStudents || !event) return 0;
        const filteredStudents: AttendeeV2[] = allStudents.filter((s) => {
            const studentCheckOutDate = new Date(s.checkOutTime);
            const eventCheckOutDate = new Date(event.checkOutTime);
            return studentCheckOutDate > eventCheckOutDate
        });

        if (count) {
            return filteredStudents.length
        } else {
            setStudents(filteredStudents)
        }
    }

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

    const timedelta = (checkInTime: Date, checkOutTime: Date) => {
        if (checkInTime.getFullYear() != 1970 && checkOutTime.getFullYear() != 1970) {
            const diffTime = checkOutTime.getTime() - checkInTime.getTime();
            const totalHours = Math.floor(diffTime / 3600000); // <- 3600000 = milliseconds
            const totalMinutes = Math.floor(diffTime / 60000);
            const diffMinutes = totalMinutes - totalHours * 60;

            if (totalHours > 24) {
                const diffDays = Math.floor(totalHours / (24));
                const diffHours = totalHours - diffDays * 24
                return diffDays + ' days ' + diffHours + 'h'
            }
            return totalHours + 'h ' + diffMinutes + 'm'
        } else {
            return "/"
        }
    }

    //Return correct status by comparing dates
    const getAttendanceStatus = (attendee: AttendeeV2) => {
        // To prevent function from throwing null reference errors
        if (!event) return "❌";

        if (attendee.validAbsence) return "☑️";

        let unexpected = !expectedStudents.has(attendee.studentNumber);
        if (unexpected) {
            return "❓"
        }

        if (event.eventType === "CheckIn") {
            if (!attendee.checkInTime) return "❌";
            return attendee.checkInTime <= event.checkInTime ? "✅" : "⏰";
        }

        if (event.eventType === "CheckOut") {
            if (!attendee.checkOutTime) return "❌";
            return attendee.checkOutTime <= event.checkOutTime ? "✅" : "⏰";
        }

        if (event.eventType === "CheckInOut") {
            if (!attendee.checkInTime || !attendee.checkOutTime) return "❌";
            return (attendee.checkInTime <= event.checkInTime) ? "✅" : "⏰";
        }

        return "❌";
    };

    const isHalfPresent = (attendee: AttendeeV2) => {
        let unexpected = !expectedStudents.has(attendee.studentNumber);
        if (unexpected) {
            return false;
        }
        const hasCheckIn = attendee.checkInTime !== null;
        const hasCheckOut = attendee.checkOutTime !== null;
           
        return hasCheckIn !== hasCheckOut; // XOR
    };


    const handleSort = (field: string) => {
        let newOrder: "asc" | "desc" | null = "asc";

        // Cycle: none → asc → desc → none
        if (sortField === field && sortOrder === "asc") newOrder = "desc";
        else if (sortField === field && sortOrder === "desc") newOrder = null;

        setSortField(newOrder ? field : null);
        setSortOrder(newOrder);

        if (!newOrder) {
            if (previousFilter === "all") filterAllStudents();
            if (previousFilter === "present") filterPresentStudents();
            if (previousFilter === "absent") filterAbsentStudents();
            if (previousFilter === "lateIn") filterLateCheckInStudents();
            if (previousFilter === "lateOut") filterLateCheckOutStudents();
            if (previousFilter === "checkInOnly") filterCheckInStudents();
            if (previousFilter === "checkOutOnly") filterCheckOutStudents();
            return;
        }

        setStudents(prev =>
            [...prev].sort((a, b) => {
                const valA = a[field] ?? "";
                const valB = b[field] ?? "";
                
                if (valA < valB) return newOrder === "asc" ? -1 : 1;
                if (valA > valB) return newOrder === "asc" ? 1 : -1;
                return 0;
            })
        );
    };

    const renderArrow = (field: string) => {
        if (sortField !== field) return null;
        if (sortOrder === "asc") return <span className="ml-1">▲</span>;
        if (sortOrder === "desc") return <span className="ml-1">▼</span>;
        return null;
    };

    // --- SEARCH ---
    const filtered = students?.filter(s => {
        const term = search.toLowerCase();
        return (
            s.lastName.toLowerCase().includes(term) ||
            s.studentNumber.toLowerCase().includes(term)
        );
    });



    return (
        <>
            <div className="flex gap-10 my-6">
                <div className="flex-1 gap-10 my-6">
                    <EventInformation />
                </div>
                {filterCounters && (
                    <div className="my-6 flex justify-center">
                        <AttendanceGauge
                            present={filterCounters.presentCount - filterCounters.unexpectedPresentCount}
                            total={filterCounters.expectedCount}
                        />
                    </div>
                )}
            </div>


            <div className="flex items-center w-[80%]">
                <div className="w-1/3"></div>
                <div className="w-1/3">
                    <input
                        type="text"
                        placeholder="Search by last name or R-number..."
                        value={search}
                        onChange={(e) => setSearch(e.target.value)}
                        className="border px-2 py-1 rounded w-full"
                    />
                </div>

                {event && <PathButton
                    path={"/courses/" + courseId + "/events/" + event.id}
                    comment="Register"
                    customClassName="bg-cats-dark-green text-white ml-1 py-1 hover:bg-black transition-colors" />
                }

                <div className="w-1/3 flex gap-2">
                    {event && (
                        <>


                            <button
                                onClick={() => setShowAddForm(!showAddForm)}
                                className="bg-cats-dark-green text-white px-3 py-1 rounded-xl hover:bg-black transition-colors ml-3"
                            >
                                {showAddForm ? "Cancel" : "Add/change attendee"}
                            </button>
                            <div className="ml-auto">
                                <GoBack url={`/courses/${courseId}/events`} isRounded />
                            </div>
                        </>

                    )}
                </div>

            </div>

            {showAddForm && (
                <AddAttendeesForm
                    eventId={eventId}
                    onAdd={(newAttendee) => {
                        setStudents(prev => [...prev, newAttendee]);
                        setAllStudents(prev => [...prev, newAttendee]);
                        setShowAddForm(false); // optionally hide the form after adding
                        setRefreshFlag(!refreshFlag);
                    }}
                    onError={(msg) => alert(msg)}
                />
            )}


            <section className="flex gap-5 my-4">

                <button
                    className="px-3 py-1 rounded-xl bg-cats-dark-green text-white py-2 hover:bg-black transition-colors"
                    onClick={() => {
                        setActiveFilter("all")
                        setPreviousFilter("all")
                        filterAllStudents()
                    }}>All ({filterCounters?.allCount})</button>
                <button
                    className="px-3 py-1 rounded-xl bg-cats-dark-green text-white py-2 hover:bg-black transition-colors"
                    onClick={() => {
                        setActiveFilter("present")
                        setPreviousFilter("present")
                        filterPresentStudents()
                    }}>Present ({filterCounters?.presentCount})</button>
                <button
                    className="px-3 py-1 rounded-xl bg-cats-dark-green text-white py-2 hover:bg-black transition-colors"
                    onClick={() => {
                        setActiveFilter("absent")
                        setPreviousFilter("absent")
                        filterAbsentStudents()
                    }}>Absent ({filterCounters && filterCounters.absentCount})</button>

                {event && (event.eventType != "CheckOut") && (<>
                    <button
                        className="px-3 py-1 rounded-xl bg-cats-dark-green text-white py-2 hover:bg-black transition-colors"
                        onClick={() => {
                            setActiveFilter("lateIn")
                            setPreviousFilter("lateIn")
                            filterLateCheckInStudents()
                        }}>Late check-in ({filterCounters && filterCounters.lateCheckInCount})</button>
                </>)}

                {event && (event.eventType != "CheckIn") && (<>
                    <button
                        className="px-3 py-1 rounded-xl bg-cats-dark-green text-white py-2 hover:bg-black transition-colors"
                        onClick={() => {
                            setActiveFilter("lateOut")
                            setPreviousFilter("lateOut")
                            filterLateCheckOutStudents()
                        }}>Late check-out ({filterCounters && filterCounters.lateCheckOutCount})</button>
                </>)}

                {event && event.eventType == "CheckInOut" && (<>
                    <button
                        className="px-3 py-1 rounded-xl bg-cats-dark-green text-white py-2 hover:bg-black transition-colors"
                        onClick={() => {
                            setActiveFilter("checkInOnly")
                            setPreviousFilter("checkInOnly")
                            filterCheckInStudents()
                        }}>Only checked-in ({filterCounters && filterCounters.checkInCount})</button>
                    <button
                        className="px-3 py-1 rounded-xl bg-cats-dark-green text-white py-2 hover:bg-black transition-colors"
                        onClick={() => {
                            setActiveFilter("checkOutOnly")
                            setPreviousFilter("checkOutOnly")
                            filterCheckOutStudents()
                        }}>Only checked-out ({filterCounters && filterCounters.checkOutCount})</button>
                </>)}
            </section>




            <table className="w-3/4 border rounded mt-2 text-left min-w-[600px]">
                <thead>
                    <tr className="bg-cats-light-green">
                        <th onClick={() => handleSort("studentNumber")} style={{ cursor: "pointer" }}>
                            Student Number {renderArrow("studentNumber")}
                        </th>

                        <th onClick={() => handleSort("firstName")} style={{ cursor: "pointer" }}>
                            First Name {renderArrow("firstName")}
                        </th>

                        <th onClick={() => handleSort("lastName")} style={{ cursor: "pointer" }}>
                            Last Name {renderArrow("lastName")}
                        </th>

                        {event && event.eventType != "CheckOut" && (
                            <th onClick={() => handleSort("checkInTime")} style={{ cursor: "pointer" }}>
                                Time of Check-in {renderArrow("checkInTime")}
                            </th>
                        )}

                        {event && event.eventType != "CheckIn" && (
                            <th onClick={() => handleSort("checkOutTime")} style={{ cursor: "pointer" }}>
                                Time of Check-out {renderArrow("checkOutTime")}
                            </th>
                        )}

                        {event && event.eventType == "CheckInOut" && (
                            <th onClick={() => handleSort("timeDelta")} style={{ cursor: "pointer" }}>
                                Time present {renderArrow("timeDelta")}
                            </th>
                        )}

                        <th>Legitimately absent?</th>
                        <th>Status</th>
                        <th>Actions</th>
                    </tr>
                </thead>

                <tbody>
                    {filtered && filtered.length > 0 && filtered.map((attendee, index) => (
                        <tr
                            key={index}
                            className={`border-t transition-colors
                            ${getAttendanceStatus(attendee) === "⏰" ? "bg-red-200" : ""}
                            ${getAttendanceStatus(attendee) === "❌" ? "bg-red-300" : ""}
                            ${getAttendanceStatus(attendee) === "❓" ? "bg-blue-200" : ""}
                            ${getAttendanceStatus(attendee) === "☑️" ? "bg-blue-50" : ""}
                            ${isHalfPresent(attendee) ? "bg-orange-200" : ""}
                            `}
                        >
                            <td className="relative group">
                                <span>{attendee.studentNumber}</span>
                                <div className="absolute bottom-full mb-2 hidden group-hover:block bg-gray-800 text-white text-xs rounded px-2 py-1 whitespace-nowrap">
                                    {studentGroupMap[attendee.studentNumber]?.join(", ") ?? "No group"}
                                </div>
                            </td>
                    
                            <td>{attendee.firstName}</td>
                            <td>{attendee.lastName}</td>
                            {event && event.eventType != "CheckOut" && (<td>{attendee.checkInTime ? new Date(attendee.checkInTime).toLocaleString() : "/"}</td>)}
                            {event && event.eventType != "CheckIn" && (<td>{attendee.checkOutTime ? new Date(attendee.checkOutTime).toLocaleString() : "/"}</td>)}
                            {event && event.eventType == "CheckInOut" && (<td>{attendee.timedeltaStr}</td>)}


                            <td>
                                {(attendee.status === "Absent" || attendee.status === "HalfPresent" || attendee.status === "Late" || attendee.status === "AbsentLate") && (
                                    <input type="checkbox" checked={attendee.validAbsence} onChange={() => handleToggleLegitimatelyAbsent(attendee)} />
                                )}
                            </td>
                            <td className="relative group">
                                <span>
                                    {getAttendanceStatus(attendee)}
                                </span>
                                <div className="absolute bottom-full mb-2 hidden group-hover:block bg-gray-800 text-white text-xs rounded px-2 py-1 whitespace-nowrap">
        
                                    {getAttendanceStatus(attendee) === "❓" ? "Unexpected attendance" :
                                        getAttendanceStatus(attendee) === "✅" ? "Present on time" :
                                            getAttendanceStatus(attendee) === "⏰" ? "Checked in/out late" :
                                                getAttendanceStatus(attendee) === "☑️" ? "Valid absence" :
                                                    "Absent"}
                                </div>
                            </td>
                            <td className="p-2">
                                <button
                                    onClick={() => handleRemove(attendee.checkInId, attendee.checkOutId)}
                                    className="bg-red-600 text-white rounded px-2 py-1 hover:bg-red-700 transition-colors">
                                    Clear
                                </button>
                            </td>
                        </tr>
                    ))}

                    {filtered && filtered.length === 0 && (
                        <tr>
                            <td colSpan={9} className="text-center py-4">
                                No students found.
                            </td>
                        </tr>
                    )}
                </tbody>
            </table>
        </>
    )
}

export default Attendees;
