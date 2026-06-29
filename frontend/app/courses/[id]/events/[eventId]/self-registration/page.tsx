'use client'
import EventInformation from "@components/attendees/eventInformation";
import {useEffect, useState} from "react";
import {useQRCode} from "next-qrcode";
import {CountdownCircleTimer} from "react-countdown-circle-timer";
import {useParams} from "next/navigation";
import GoBack from "@components/goBack";
import { changeEventRotationCodeAction } from "actions/eventActions";

const frontEndUrl = process.env.NEXT_FRONT_END_URL;

const SelfRegistration = () => {
    // const [type, setType] = useState<string>('Checkin')
    const [rotatingCode, setRotatingCode] = useState<string>()
    const [eventType, setEventType] = useState<string>("Checkin")
    const { Canvas } = useQRCode();

    const params = useParams<{ eventId: string; id: string}>();
    const eventId = Number(params.eventId);
    const courseId = Number(params.id);


    useEffect(() => {
        getNewRotationCode()
    }, []);

    const getNewRotationCode = async () => {
        const rotationCode = await changeEventRotationCodeAction(eventId)
        setRotatingCode(rotationCode)
    }

    return (

        <>
            <GoBack url={`/courses/${courseId}/events`} isRounded />

            <EventInformation />

            <div className="mb-2">
                <label className="block mb-2 mt-2 mr-2 font-semibold">Badge Type:</label>
                <select
                    value={eventType}
                    onChange={(e) => {setEventType(e.target.value)}}
                    className="border rounded px-2 py-1 w-full"
                >
                    <option value="Checkin">Checkin</option>
                    <option value="Checkout">Checkout</option>

                </select>
            </div>

            <a href={window.location + `/${eventType}/student-form`}>URL</a>

            <section className="flex justify-around w-[1000px]">
                <Canvas
                    // text={frontEndUrl + `/courses/${courseId}/events/${eventId}/self-registration-student-form`}
                    text={window.location + `/${eventType}/student-form`}
                    options={{
                        errorCorrectionLevel: 'L',
                        margin: 2,
                        scale: 5,
                        width: 400,
                    }}
                />

                {rotatingCode && (<CountdownCircleTimer
                    isPlaying
                    duration={30}
                    colors={['#004777', '#004777']}
                    colorsTime={[20, 5]}
                    size={400}
                    onComplete={() => {
                        // do your stuff here
                        const previousCode = rotatingCode
                        getNewRotationCode()

                        return {shouldRepeat: true, delay: 0} // repeat animation in 1.5 seconds
                    }}
                >
                    {({ remainingTime }) => <p className="text-[90px]">{rotatingCode}</p>}
                </CountdownCircleTimer>)}

            </section>




        </>
    )
}

export default SelfRegistration;
