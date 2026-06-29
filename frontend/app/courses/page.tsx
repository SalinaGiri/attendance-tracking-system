import CourseForm from "@components/courses/courseForm"

export const metadata = { title: "Add Course", };

const AddCourse = () => {
    return (
        <>
            <main className="text-center md:mt-24 mx-auto md:w-3/5 lg:w-1/2">
                <section className="mt-5">
                    {<CourseForm courseName="Course name: " courseDescription="Description: " />}
                </section>
            </main>
        </>
    )
}
export default AddCourse;
