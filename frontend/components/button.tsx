type Props = React.ButtonHTMLAttributes<HTMLButtonElement> & {
    children: string
}

const Button: React.FC<Props> = ({ children, ...rest }) => {
    return (
        <>
            <button
                className="text-white bg-blue-700 hover:bg-blue-800 font-medium rounded-lg text-sm px-5 py-2.5 text-center"
                {...rest}
            >
                {children}
            </button>
        </>
    )
}

export default Button
