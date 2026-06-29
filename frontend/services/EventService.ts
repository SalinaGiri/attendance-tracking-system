import { Event, EventInput, Group } from "@types";
import { FetchError } from "utils/errors";

const apiUrl = process.env.NEXT_PUBLIC_API_URL;

const getAll = async () => {
  try {
    return await fetch(apiUrl + '/events', {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
    });
  } catch (error) {
    throw new FetchError("Fetch Error when fetching events.");
  }
}

const getEvent = async (eventId): Promise<Event> => {
  try {
    const response = await fetch(apiUrl + `/events/${eventId}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
    });

    const result = await response.json();
    return result;
  } catch (error) {
    throw new FetchError("Fetch Error when fetching events.");
  }
}

const getEventGroups = async (eventId): Promise<Group[]> => {
  try {
    const response = await fetch(apiUrl + `/events/${eventId}/groups`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
    });

    const result = await response.json();
    return result;
  } catch (error) {
    throw new FetchError("Fetch Error when fetching events.");
  }
}

const addEvent = async (payload: EventInput) => {
  let response = null;
  try {
    response = await fetch(apiUrl + "/events/add_event", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(payload)
    });
  } catch (error) {
    throw new FetchError("Fetch Error when fetching addEvent.");
  }

  if (!response.ok) {
    const resJSON = await response.json();
    throw new Error(resJSON["Error"]);
  }

  return response.json();
}

const changeEventRotationCode = async (id: number) => {
    let response = null;
    try {
        response = await fetch(apiUrl + `/events/${id}/rotation-code`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            }
        });
    } catch (error) {
        throw new FetchError("Fetch Error when changing rotation code.");
    }

    if (!response.ok) {
        const resJSON = await response.json();
        throw new Error(resJSON["Error"]);
    }

    return response.json();
}

const EventService = {
    getAll,
    getEvent,
    addEvent,
    changeEventRotationCode,
    getEventGroups,
  }
  
  export default EventService;
