import AddEventOverview from '@components/events/AddEventOverview'
import Head from 'next/head'

export const metadata = { title: "Add an event", };

type PageProps = {
  params: Promise<{ id: string }>;
};

async function events({ params }: PageProps) {
  const { id } = await params;
  const courseId = Number(id)
  return (
    <>
      <Head>
        <title>Events</title>
      </Head>

      <main className="mx-8 py-4 px-6 bg-cats-light-green rounded-xl">
        <h1 className="text-center mb-2 font-bold text-white bg-cats-dark-green rounded">Add Event</h1>
        <section className="mt-5">
          <AddEventOverview courseId={courseId} />
        </section>
      </main>
    </>
  )
}

export default events
