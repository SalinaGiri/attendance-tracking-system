import Header from '@components/header';
import '@styles/globals.css';
import "react-datepicker/dist/react-datepicker.css";
import "@styles/utils.css";

const RootLayout = async ({
  children,
  params,
}: {
  children: React.ReactNode;
  params: Promise<{ locale: string }>;
}) => {
  const { locale } = await params;

  return (
    <html lang={locale}>
      <body>
        <Header />
        <main className="p-6 min-h-screen flex flex-col items-center">
          {children}
        </main>
      </body>
    </html>
  );
};

export default RootLayout;
