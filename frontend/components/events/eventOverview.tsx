'use client'

import { Event, EventInput } from "@types";
import Link from "next/link";
import { Group } from "@types";
import PathButton from '@components/urlButton';
import GoBack from "@components/goBack";


type Props = {
  events: Event[]
  courseId: number | null;
}

const EventOverview: React.FC<Props> = ({ events, courseId }: Props) => {


  const eventsJSON = events.map((e: any) => ({
    ...e,
    checkInTime: new Date(e.checkInTime),
    checkOutTime: new Date(e.checkOutTime),
  }));

  return (
    <>
      <section className="flex justify-center gap-10">
        <Link className="flex items-center" href={"/courses/" + courseId + "/events/add_event"}>
          <button
            className="px-3 py-1 rounded-xl text-black block bg-cats-medium-white hover:bg-white transition-colors"
          >
            Add Event</button>
        </Link>
        <div className="flex items-center">
          <GoBack url="/" isRounded />
        </div>


      </section>
      <section className="">
        <table className="bg-cats-medium-white mt-5 rounded-xl hover:rounded-xl mx-auto">
          {eventsJSON && eventsJSON.length > 0 &&
            <thead className="rounded-xl bg-cats-dark-white">
              <tr className="rounded-xl">
                <th scope="col">Event Name</th>
                <th scope="col">Check-in Time</th>
                <th scope="col">Check-out Time</th>
                <th scope="col">Group</th>
                <th scope="col">Action</th>
                <th scope="col">Attendees</th>
                <th scope="col">Self registration</th>
              </tr>
            </thead>
          }
          <tbody>
            {eventsJSON &&
              eventsJSON.length > 0 &&
              eventsJSON.map((event, index) => (
                <tr key={index}>
                  <td>
                    {event.eventName}
                  </td>
                  <td>
                    {event.checkInTime.getFullYear() != 1970 && (
                      `${event.checkInTime.getDate()}/${event.checkInTime.getMonth() + 1}/${event.checkInTime.getFullYear()}
                        ${event.checkInTime.getHours()}:${event.checkInTime.getMinutes().toString().padStart(2, "0")}`
                    )}

                    {event.checkInTime.getFullYear() == 1970 && ('/')}
                  </td>
                  <td>
                    {event.checkOutTime.getFullYear() != 1970 && (
                      `${event.checkOutTime.getDate()}/${event.checkOutTime.getMonth() + 1}/${event.checkOutTime.getFullYear()}
                            ${event.checkOutTime.getHours()}:${event.checkOutTime.getMinutes().toString().padStart(2, "0")}`
                    )}

                    {event.checkOutTime.getFullYear() == 1970 && ('/')}
                  </td>
                  <td>{event.groups.map((g: Group) => g.name).join(", ")}</td>
                  <td>
                    <PathButton
                      path={"/courses/" + courseId + "/events/" + event.id}
                      comment="Register"
                      customClassName="bg-cats-dark-green block text-white py-0.5 hover:bg-black transition-colors" />
                  </td>
                  <td>
                    <PathButton
                      path={"/courses/" + courseId + "/events/" + event.id + "/attendees"}
                      comment="See attendees"
                      customClassName="bg-cats-dark-green block text-white py-0.5 hover:bg-black transition-colors" />
                  </td>
                    <td>
                        <PathButton
                            path={"/courses/" + courseId + "/events/" + event.id + "/self-registration"}
                            comment="Self registration"
                            customClassName="bg-cats-dark-green block text-white py-0.5 hover:bg-black transition-colors" />
                    </td>
                </tr>
              ))}
          </tbody>
        </table>
        {(!eventsJSON || eventsJSON.length === 0) && <p>No events present for this course.</p>}
      </section>
    </>
  )
}

export default EventOverview;