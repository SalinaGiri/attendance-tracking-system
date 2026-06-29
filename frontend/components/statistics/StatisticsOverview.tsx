import { AttendeeProjection, AttendeeV2 } from "@types"
import GoBack from "@components/goBack";


type Props = {
    registrations: AttendeeProjection[];
}

const StatisticsOverview: React.FC<Props> = ({registrations}: Props) => {

    const filteredRegistrations = registrations.filter(reg => 
        (reg.absenceCount ?? 0) +
        (reg.halfPresentCount ?? 0) +
        (reg.absentLateCount) + 
        (reg.lateCount ?? 0) > 1
    );
    
    return (
        <>
            <section className="flex justify-center gap-10">
                <div className="flex items-center">
                    <GoBack url="/" isRounded />
                </div>
            </section>
            {filteredRegistrations.length > 0 && <section>
                <table className="bg-cats-medium-white mt-5 rounded-xl hover:rounded-xl mx-auto">
                    <thead>
                        <tr className="rounded-xl">
                            <th scope="col">Student number</th>
                            <th scope="col">First name</th>
                            <th scope="col">Last name</th>
                            <th scope="col">Absence count</th>
                            <th scope="col">Legitimate absence count</th>
                            <th scope="col">Half present count</th>
                            <th scope="col">Late count</th>
                            <th scope="col">Late + absent count</th>
                        </tr>
                    </thead>
                    <tbody>
                        {filteredRegistrations.map((registration, index) => (
                            <tr key={index}>
                                <td>{registration.studentNumber}</td>
                                <td>{registration.firstName}</td>
                                <td>{registration.lastName}</td>
                                <td>{registration.absenceCount}</td>
                                <td>{registration.validAbsenceCount}</td>
                                <td>{registration.halfPresentCount}</td>
                                <td>{registration.lateCount}</td>
                                <td>{registration.absentLateCount}</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </section> }
            {filteredRegistrations.length === 0 && <p>No registrations with patterns found.</p>}
        </>
    )
}

export default StatisticsOverview;