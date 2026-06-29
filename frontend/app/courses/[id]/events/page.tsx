import EventOverview from '@components/events/eventOverview';
import CourseService from '@services/CourseService';
import Head from 'next/head';
import { Event } from '@types';

export const metadata = { title: "Events", };

type Props = {
  events: Event[] | null;
  courseId: number | null;
}

const getEvents = async (id: number): Promise<Props> => {
  try {

    const eventsFetched = await CourseService.findEventsByCourseId(id);

    return { events: eventsFetched, courseId: id };
  } catch (error) {
    return { events: null, courseId: null };
  }
};

const EventsPage = async ({ params }: { params: Promise<{ id: string }> }) => {
  const { id: idString } = await params;
  const id = Number(idString)
  const { events, courseId } = await getEvents(id);


  return (
    <>
      <Head>
        <title>Events</title>
      </Head>
      <h1 className="text-black text-xl pb-5 text-center">Events</h1>

      <main className="mx-[150px] py-4 px-6 bg-cats-light-green rounded-xl">

        <section className="">
          {events && <EventOverview events={events} courseId={courseId} />}
        </section>
      </main>
    </>
  )
}

export default EventsPage;