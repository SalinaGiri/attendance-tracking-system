"use server"

import RegistrationService from "@services/RegistrationService"
import { SelfRegistration } from "@types";

export async function fetchFilteredAttendeesAction(eventId: string, filter: "unexpected" | "present" | "absent" | "late", registrationType: string) {
  if (!eventId ) throw new Error("missing eventId")
  return RegistrationService.getFilteredRegistrations(eventId, filter, registrationType)
}

export async function uploadRegistrationAction(file: File, type: string, eventId: number) {
  if (!file || !type || !eventId) throw new Error("Missing file, type or eventId");
  return RegistrationService.upload(file, type, eventId);
}

export async function toggleLegitimatelyAbsentAction(registrationId:number, mainId: boolean) {
  const response = await RegistrationService.toggleLegitimatelyAbsent(registrationId, mainId);
  return {response:"success"}
}

export async function deleteRegistrationAction(registrationId:number) {
  if (!registrationId) throw new Error("missing registrationId");
  return RegistrationService.deleteRegistration(registrationId);
}

export async function getAllByEventId(eventId:number) {
    if (!eventId) throw new Error("missing eventId");
    const response = await RegistrationService.getAllByEventIdV2(eventId);
    return response;
}

export async function getAllByCourseId(courseId: string) {
  if (!courseId) throw new Error("missing courseId");
  const response = await RegistrationService.getRegistrationsByCourseId(courseId);
  return response;
}

export async function addRegistrationManualAction(eventId:number,payload:any) {
  const response = await RegistrationService.addManual(eventId,payload);
  return response;
}

export async function selfRegisterAction(eventId:number, rNumber:string, rotatingCode:string, eventType:string) {
  const selfRegistration: SelfRegistration =  {
    eventId: eventId,
    rNumber: rNumber,
    rotatingCode: rotatingCode,
    registrationType: eventType
  }
  const response = await RegistrationService.selfRegister(selfRegistration);
  return response;
}