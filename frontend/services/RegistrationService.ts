import type {Attendee, AttendeeProjection, AttendeeV2, SelfRegistration} from "@types";
import { FetchError } from "utils/errors";
import {fetchEventByEventIdAction} from "../actions/eventActions";
import {responseCookiesToRequestCookies} from "next/dist/server/web/spec-extension/adapters/request-cookies";
const apiUrl = process.env.NEXT_PUBLIC_API_URL;


const upload = async (file: File, type: string, eventId: number) => {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('type', type)
  formData.append('eventId', String(eventId))

  const response = await fetch(apiUrl + '/registrations/upload', {
    method: 'POST',
    body: formData, // Do not set Content-Type; browser sets correct multipart boundary
  })

  return response.json()
}

// Fetch filtered attendees by event ID and filter type ("present" or "absent")
const getFilteredRegistrations = async (
  eventId: string,
  filter: "unexpected" | "present" | "absent" | "late",
  registrationType: string
): Promise<Attendee[]> => {
  const response = await fetch (`${apiUrl}/registrations/${eventId}/filter?filter=${filter}&registration-type=${registrationType}`, { 
    headers: { "Content-Type": "application/json" },
  });
  return response.json();
};

const getRegistrationsByCourseId = async (
    courseId: string
): Promise<AttendeeProjection[]> => {
    let response = null;
    try {
        response = await fetch(`${apiUrl}/registrations/v2/${courseId}`, {
        headers: {"Content-Type": "applications/json"},
    });
    } catch (error) {
        throw new FetchError("FetchError when getting registrations by course id.");
    }
    if (!response.ok) {
        const resJSON = await response.json();
        throw new Error(resJSON["Error"]);
    }
    return response.json();
}

const toggleLegitimatelyAbsent = async (registrationId: number, mainId: boolean) => {
    let response = null;
    try {
        response = await fetch(apiUrl + `/registrations/${registrationId}/absence?mainId=${mainId}`, {
            method: "PATCH",
            headers: {
                "Content-Type": "application/json"
            }
            // body: JSON.stringify(payload)
        });
    } catch (error) {
        throw new FetchError("Error when toggling registration valid absence.");
    }

    if (!response.ok) {
        const resJSON = await response.json();
        throw new Error(resJSON["Error"]);
    }

    return response;
    // return `Student's legitimately absent state was toggled. (${rnumber})`
}

const deleteRegistration = async (registrationId: number) => {
    let response = null;
    try {
        response = await fetch(apiUrl + `/registrations/${registrationId}`, {
            method: "DELETE",
            headers: {
                "Content-Type": "application/json"
            }
        });
    } catch (error) {
        throw new FetchError("Error when deleting registration.");
    }

    if (!response.ok) {
        const resJSON = await response.json();
        throw new Error(resJSON["Error"]);
    }
}

const getAllByEventIdV2 = async (eventId: number)=> {
    let response = null;

    try {
        response = await fetch(apiUrl + `/registrations/v2/${eventId.toString()}/all`)
    } catch (e) {
        throw new FetchError("Error when fetching all registrations by event.")
    }

    if (!response.ok) {
        const resJSON = await response.json();
        throw new Error(resJSON["Error"]);
    }

    return response.json();
}

 const addManual = async (eventId: number, payload: any): Promise<AttendeeV2> => {
    try {
        const response = await fetch(`${apiUrl}/registrations/${eventId}/manual`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(payload)
        });
        const responseJson = await response.json();
        return responseJson;
    } catch (error: any) {
        console.log(error);
        throw new Error(error.response?.data || "Failed to add attendee");
    }
};

const selfRegister = async (payload: SelfRegistration) => {
    let response = null;
    try {
        response = await fetch(apiUrl + "/registrations/self-registration", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(payload)
        });
        const responseJson = await response.json();
        return responseJson;
    } catch (error) {
        throw new FetchError("Fetch Error when fetching selfRegister.");
    }

}



const RegistrationService = {
  upload, 
  getFilteredRegistrations,
  getRegistrationsByCourseId,
  toggleLegitimatelyAbsent, 
  deleteRegistration,
    getAllByEventIdV2,
    addManual,
    selfRegister
}

export default RegistrationService