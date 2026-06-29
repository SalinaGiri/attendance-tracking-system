"use client";
import { useRouter } from "next/navigation";

type Props = {
    url: string
    isRounded?: boolean
}

// Usage: <GoBack url="" /> or <GoBack url="" isRounded /> for rounded corners 
const GoBack = ({ url, isRounded = false }: Props) => {
    const router = useRouter();
    const borderType = isRounded ? "rounded-xl" : "rounded"

    return <>
        <button
            type="button"
            className={`bg-gray-500 text-white ${borderType} px-3 py-1 hover:bg-gray-600 transition-colors`}
            onClick={() => router.push(url)}
        >
            Go Back
        </button>
    </>
}

export default GoBack;