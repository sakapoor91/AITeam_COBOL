import './globals.css';
import Sidebar from './sidebar';

export const metadata = {
  title: 'EvolutionAI Dashboard',
  description: 'Legacy Modernization Metrics Dashboard',
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <body className="bg-slate-50">
        <div className="flex min-h-screen">
          <Sidebar />
          <main className="ml-64 flex-1 bg-slate-50 min-h-screen p-8">
            {children}
          </main>
        </div>
      </body>
    </html>
  );
}
