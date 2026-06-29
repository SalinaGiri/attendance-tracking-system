"use server"

import EventService from "@services/EventService"
import { EventInput } from "@types"

export async function createEventAction(eventInput: EventInput) {
  if (!eventInput) throw new Error("missing eventInput")
  return EventService.addEvent(eventInput)
}

export async function fetchEventByEventIdAction(eventId: number) {
  if (!eventId) throw new Error("missing eventId")
  return EventService.getEvent(eventId);
}

export async function fetchGroupsByEvent(eventId: number) {
  if (!eventId) throw new Error("missing eventId")
  return EventService.getEventGroups(eventId);
}

export async function changeEventRotationCodeAction(eventId: number) {
  if (!eventId) throw new Error("missing eventId")
  return EventService.changeEventRotationCode(eventId);
}