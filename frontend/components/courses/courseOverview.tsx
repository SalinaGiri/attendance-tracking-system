import PathButton from '@components/urlButton';
import { Course } from '@types'

type Props = {
    courses: Course[];
}

const CourseOverview: React.FC<Props> = ({ courses }: Props) => {
    return (
        <>
            {/*TODO: Make this table scalable.*/}
            <table className="bg-cats-medium-white mt-5 overflow-hidden rounded-xl hover:rounded-xl mx-auto">
                <tbody>
                    {courses &&
                        courses.length > 0 &&
                        courses.map((course, index) => (
                            <tr className="border-0 hover:bg-cats-medium-white transition-colors" key={index}>
                                <td><p className="text-lg text-center">{course.name}</p></td>
                                <td><p>{course.description}</p></td>
                                <td className="flex gap-2">
                                    <PathButton
                                        path={`/courses/${course.id}/events`}
                                        comment="Events"
                                        customClassName="bg-cats-dark-green text-white px-6 py-1 rounded-lg hover:bg-black transition-colors"
                                    />
                                    <PathButton
                                        path={`/courses/${course.id}`}
                                        comment="Groups"
                                        customClassName="bg-cats-dark-green text-white px-6 py-1 rounded-lg hover:bg-black transition-colors"
                                    />
                                    <PathButton
                                        path={`/courses/${course.id}/statistics`}
                                        comment="Statistics"
                                        customClassName="bg-cats-dark-green text-white px-6 py-1 rounded-lg hover:bg-black transition-colors"
                                        />
                                </td>
                            </tr>
                        ))}
                </tbody>
            </table>
        </>
    )
}

export default CourseOverview
