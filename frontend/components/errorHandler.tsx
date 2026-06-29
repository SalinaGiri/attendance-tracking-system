import { useRouter } from "next/navigation";
import { useEffect, useState } from "react"

type Props = | {
    message: string;
    fallback: false;
    fallbackPath?: string;
    error?: Error | string;
    fetchFunction?: () => Promise<any>;
} | {
    message: string;
    fallback: true;
    fallbackPath: string;
    error?: Error | string;
    fetchFunction?: () => Promise<any>;
}

const ErrorHandler: React.FC<Props> = ({
    message,
    fallback,
    fallbackPath,
    error,
    fetchFunction
}: Props) => {
    const [isError, setIsError] = useState(false);
    const [errorMessage, setErrorMessage] = useState("");
    const [data, setData] = useState<any>(null);

    const router = useRouter();

    useEffect(() => {
        if (error) {
            setIsError(true);
            setErrorMessage(typeof error === "string" ? error : error.message);
        }
    }, [error]);

    useEffect(() => {
        if (!fetchFunction) return;
        let isMounted = true;

        fetchFunction()
            .then((response) => isMounted && setData(response))
            .catch((e) => {
                if (isMounted) {
                    setIsError(true);
                    setErrorMessage(e.message || message);
                }
            });
        return () => { isMounted = false; };
    }, [fetchFunction, message]);

    useEffect(() => {
        if (isError && fallback) {
            router.push(fallbackPath!);
        }
    }, [isError, fallback, fallbackPath, router]);

    if (isError) return <div>Error: {errorMessage}</div>;
    return <div>{data ? JSON.stringify(data) : "Loading..."}</div>
}

export default ErrorHandler
