"use client"

import Head from 'next/head'
import { useEffect, useState } from 'react'
import type { StatusMessage } from '@types'
import { useParams } from 'next/navigation'
import { uploadRegistrationAction } from 'actions/registrationActions'
import { useRouter } from "next/navigation";
import GoBack from '@components/goBack'

const RegistrationsPage = () => {
  useEffect(() => {
    document.title = "Upload student badging";
  }, []);
  const router = useRouter();
  const [files, setFiles] = useState<File[]>([])
  const [type, setType] = useState<string>('Checkin')
  const [status, setStatus] = useState<StatusMessage | null>(null)
  const [isUploading, setIsUploading] = useState<boolean>(false)
  const [uploadProgress, setUploadProgress] = useState<string>('')

  const params = useParams<{ id: string; eventId: string }>()
  const courseId = Number(params.id)
  const eventId = params.eventId

  const onFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const fileList = e.target.files
    if (!fileList || fileList.length === 0) {
      setFiles([])
      return
    }

    // Validate all files
    const validFiles: File[] = []
    const invalidFiles: string[] = []

    for (let i = 0; i < fileList.length; i++) {
      const f = fileList[i]
      const valid = /\.(xlsx|xls)$/i.test(f.name)
      if (valid) {
        validFiles.push(f)
      } else {
        invalidFiles.push(f.name)
      }
    }

    if (invalidFiles.length > 0) {
      setStatus({ 
        type: 'error', 
        message: `Invalid files (only .xlsx, .xls allowed): ${invalidFiles.join(', ')}` 
      })
      setFiles([])
      return
    }

    setStatus(null)
    setFiles(validFiles)
  }

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setStatus(null)
    setUploadProgress('')

    if (files.length === 0) {
      setStatus({ type: 'error', message: 'Please select at least one Excel file.' })
      return
    }
    if (!type?.trim()) {
      setStatus({ type: 'error', message: 'Please provide a type.' })
      return
    }
    if (!eventId || Array.isArray(eventId)) {
      setStatus({ type: 'error', message: 'Invalid or missing eventId in the route.' })
      return
    }

    try {
      setIsUploading(true)
      const results: { file: string; success: boolean; message: string }[] = []

      // Upload files sequentially
      for (let i = 0; i < files.length; i++) {
        const file = files[i]
        setUploadProgress(`Uploading file ${i + 1} of ${files.length}: ${file.name}`)

        try {
          const res = await uploadRegistrationAction(file, type.trim(), Number(eventId))
          if (!res.message) {
            const errText = await res.text().catch(() => 'Upload failed.')
            results.push({ 
              file: file.name, 
              success: false, 
              message: errText || 'Upload failed.' 
            })
          } else {
            results.push({ 
              file: file.name, 
              success: true, 
              message: 'Uploaded successfully.' 
            })
          }
        } catch (err) {
          results.push({ 
            file: file.name, 
            success: false, 
            message: 'Network or server error during upload.' 
          })
        }
      }

      // Check if all succeeded
      const allSuccess = results.every(r => r.success)
      const someSuccess = results.some(r => r.success)

      if (allSuccess) {
        setStatus({ 
          type: 'success', 
          message: `All ${files.length} file(s) uploaded successfully.` 
        })
        setFiles([])
        ; (document.getElementById('file-input') as HTMLInputElement | null)?.value && ((document.getElementById('file-input') as HTMLInputElement).value = '')
        setTimeout(() => { router.push("/courses/" + courseId + "/events/" + eventId + "/attendees") }, 2000)
      } else if (someSuccess) {
        const failed = results.filter(r => !r.success)
        setStatus({ 
          type: 'error', 
          message: `${failed.length} file(s) failed to upload: ${failed.map(f => f.file).join(', ')}` 
        })
      } else {
        setStatus({ 
          type: 'error', 
          message: `All ${files.length} file(s) failed to upload.` 
        })
      }
    } catch (err) {
      setStatus({ type: 'error', message: 'Unexpected error during upload process.' })
    } finally {
      setIsUploading(false)
      setUploadProgress('')
    }
  }

  return (
    <>
      <Head>
        <title>Registrations</title>
      </Head>
      <h1 className="text-center pb-2">Registrations</h1>
      <main className="mx-[400px] py-4 px-6 bg-cats-light-green rounded-xl  ">

        <section className="">
          <form onSubmit={onSubmit} className="">
            <div>
              <label htmlFor="file-input" className="text-sm">
                Excel file(s) (.xlsx or .xls) - Select one or multiple files
              </label>
              <input
                id="file-input"
                name="file"
                type="file"
                multiple
                accept=".xlsx,.xls,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,application/vnd.ms-excel"
                onChange={onFileChange}
                className="mt-3 block w-full text-sm file:mr-4 file:py-2 file:px-4
                  file:rounded-md file:border-0 file:text-sm
                  file:bg-gray-100 file:text-gray-700 hover:file:bg-gray-200"
              />
              {files.length > 0 && (
                <p className="text-xs text-gray-600 mt-1">
                  {files.length} file(s) selected
                </p>
              )}
            </div>

            <div className="mb-2">
              <label className="block mb-2 mt-2 mr-2 font-semibold">Badge Type:</label>
              <select
                value={type}
                onChange={(e) => setType(e.target.value)}
                className="border rounded px-2 py-1 w-full"
              >
                <option value="Checkin">Checkin</option>
                <option value="Checkout">Checkout</option>

              </select>
            </div>

            {uploadProgress && (
              <div className="rounded-md p-3 text-sm bg-blue-100 text-blue-800 mb-2">
                {uploadProgress}
              </div>
            )}

            {status && (
              <div
                className={`rounded-md p-3 text-sm ${status.type === 'success' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
                  }`}
              >
                {status.message}
              </div>
            )}

            <div className="flex justify-items gap-4">
              <button
                type="submit"
                disabled={isUploading}
                className="bg-cats-dark-green text-cats-medium-white rounded-xl py-1 px-10 hover:bg-black transition-colors"
              >
                {isUploading ? 'Uploading...' : `Upload ${files.length || ''} Excel File${files.length !== 1 ? 's' : ''}`}
              </button>
              <GoBack url={`/courses/${courseId}/events`} isRounded />
            </div>
          </form>
        </section>
      </main>
    </>
  )
}

export default RegistrationsPage