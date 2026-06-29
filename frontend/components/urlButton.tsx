'use client'

import Link from "next/link";

type Props = {
    path: string;
    comment: string;
    customClassName?: string;
}

const PathButton: React.FC<Props> = ({ path, comment, customClassName }: Props) => {
    return (
        <Link
          href={path}
          className={"px-5 rounded-xl" + " " + customClassName}
        >
          {comment}
        </Link>
    )
}

export default PathButton
