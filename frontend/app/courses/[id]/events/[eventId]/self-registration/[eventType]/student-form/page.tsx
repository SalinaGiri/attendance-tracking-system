'use client'
import EventInformation from "@components/attendees/eventInformation";
import {useState} from "react";
import {useParams} from "next/navigation";
import GoBack from "@components/goBack";
import { selfRegisterAction } from "actions/registrationActions";

const apiUrl = process.env.NEXT_PUBLIC_API_URL;

const Attendees = () => {
    const params = useParams<{ eventId: string; id: string; eventType: string }>();
    const eventId = Number(params.eventId);
    const eventType = String(params.eventType)
    const courseId = Number(params.id)
    const [rotatingCode, setRotatingCode] = useState<string>()
    const [rNumber, setRNumber] = useState<string>()
    const [status, setStatus] = useState<string>()



    const handleRegistration = async () => {
        try {
            const response = await selfRegisterAction(eventId, rNumber, rotatingCode, eventType)

            setStatus(response)
        }
        catch (error) {
            setStatus("something went wrong")
        }
    }

    return (

        <>
            <GoBack url={`/courses/${courseId}/events/${eventId}/self-registration/`} isRounded />

            <EventInformation/>

            <form className="mt-5 text-center flex flex-col" action="/action_page.php">
                <label className="" htmlFor="fname">rNumber:</label>
                <input className="border-gray-200 border-2 mt-2 inline-block w-[150px]" type="text" id="fname" name="fname" placeholder="r1234567"
                       value={rNumber}
                       onChange={(e) => setRNumber(e.target.value)}/>

                <label className="mt-5" htmlFor="fname">Code:</label>
                <input className="border-gray-200 border-2 mt-2 inline-block w-[150px]" type="text" id="fname" name="fname" placeholder="code"
                       value={rotatingCode}
                       onChange={(e) => setRotatingCode(e.target.value)}/>

                <button className="bg-cats-dark-green text-white rounded-xl px-2 mt-5" type="button"
                        onClick={handleRegistration}>Register</button>
            </form>

            <p className={"mt-5" + " " + (status == "Success." ? "text-green-500" : "text-red-500") }>{status}</p>

        </>
    )
}

export default Attendees;
