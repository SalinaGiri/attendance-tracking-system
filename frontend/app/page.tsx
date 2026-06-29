import Head from "next/head"
import CourseService from "@services/CourseService"
import CourseOverview from "@components/courses/courseOverview"
import PathButton from "@components/urlButton"
import { Course } from "@types"

export const dynamic = "force-dynamic";

export const metadata = { title: "CATS", };

type CourseResult = {
  courses: Course[] | null;
};

const getCourses = async (): Promise<CourseResult> => {
  try {
    const coursesfetched = await CourseService.getAll();

    return { courses: coursesfetched };
  } catch (error) {
    return { courses: null };
  }
};

const CoursePage = async () => {
  const { courses } = await getCourses();

  return (
    <>
      <Head>
        <title>CATS</title>
        <meta name="description" content="Exam app" />
        <meta name="viewport" content="width=device-width, initial-scale=1" />
        <link rel="icon" href="/favicon.ico" />
      </Head>
      <main className="mx-[150px] py-4 px-6 bg-cats-light-green rounded-xl">

        <section className="">


          {courses && courses.length > 0 ? (
            <>
            <div className="flex justify-center gap-10">
              <PathButton path="/courses" comment="Add Course" customClassName="text-black mx-auto block bg-cats-medium-white hover:bg-white transition-colors" />
            </div>
              <CourseOverview courses={courses} />
            </>
          ) : (<p>Something went wrong while trying to fetch data from the backend.</p>)}
        </section>
      </main>
    </>
  )
}

export default CoursePage

