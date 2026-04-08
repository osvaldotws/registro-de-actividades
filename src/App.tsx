import React, { useState, useEffect } from 'react';
import { format, parseISO, addDays, subDays } from 'date-fns';
import { es } from 'date-fns/locale';
import { Plus, Minus, Calendar as CalendarIcon, ChevronLeft, ChevronRight, Activity, Trash2, History, X } from 'lucide-react';
import { cn } from './lib/utils';

type ActivityItem = {
  id: string;
  name: string;
  count: number;
  note?: string;
};

type DailyData = {
  [date: string]: ActivityItem[];
};

export default function App() {
  // Initialize with today's date in YYYY-MM-DD format (local time)
  const [selectedDate, setSelectedDate] = useState(() => {
    const today = new Date();
    const offset = today.getTimezoneOffset() * 60000;
    return new Date(today.getTime() - offset).toISOString().split('T')[0];
  });

  const [data, setData] = useState<DailyData>({});
  const [newActivityName, setNewActivityName] = useState('');
  const [isLoaded, setIsLoaded] = useState(false);
  const [isHistoryOpen, setIsHistoryOpen] = useState(false);

  // Load data from LocalStorage on mount
  useEffect(() => {
    const savedData = localStorage.getItem('activityTrackerData');
    if (savedData) {
      try {
        setData(JSON.parse(savedData));
      } catch (e) {
        console.error('Failed to parse saved data', e);
      }
    }
    setIsLoaded(true);
  }, []);

  // Save data to LocalStorage whenever it changes
  useEffect(() => {
    if (isLoaded) {
      localStorage.setItem('activityTrackerData', JSON.stringify(data));
    }
  }, [data, isLoaded]);

  const currentActivities = data[selectedDate] || [];

  const handleAddActivity = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newActivityName.trim()) return;

    const newActivity: ActivityItem = {
      id: crypto.randomUUID(),
      name: newActivityName.trim(),
      count: 0,
    };

    setData((prev) => ({
      ...prev,
      [selectedDate]: [...(prev[selectedDate] || []), newActivity],
    }));
    setNewActivityName('');
  };

  const updateCount = (id: string, delta: number) => {
    setData((prev) => ({
      ...prev,
      [selectedDate]: prev[selectedDate].map((act) =>
        act.id === id ? { ...act, count: Math.max(0, act.count + delta) } : act
      ),
    }));
  };

  const updateNote = (id: string, note: string) => {
    setData((prev) => ({
      ...prev,
      [selectedDate]: prev[selectedDate].map((act) =>
        act.id === id ? { ...act, note } : act
      ),
    }));
  };

  const deleteActivity = (id: string) => {
    setData((prev) => ({
      ...prev,
      [selectedDate]: prev[selectedDate].filter((act) => act.id !== id),
    }));
  };

  const changeDate = (days: number) => {
    const dateObj = parseISO(selectedDate);
    const newDate = days > 0 ? addDays(dateObj, days) : subDays(dateObj, Math.abs(days));
    setSelectedDate(format(newDate, 'yyyy-MM-dd'));
  };

  // Format date for display (e.g., "7 de abril 2026")
  const displayDate = format(parseISO(selectedDate), "d 'de' MMMM yyyy", { locale: es });

  // Get history dates sorted from newest to oldest
  const historyDates = Object.keys(data)
    .filter(date => data[date] && data[date].length > 0)
    .sort((a, b) => b.localeCompare(a));

  if (!isLoaded) return null;

  return (
    <div className="min-h-screen bg-gray-50 text-gray-900 font-sans selection:bg-emerald-200">
      <div className="max-w-md mx-auto bg-white min-h-screen shadow-xl sm:border-x border-gray-200 flex flex-col relative overflow-hidden">
        
        {/* Header */}
        <header className="bg-emerald-600 text-white p-4 shadow-md z-10 sticky top-0">
          <div className="flex items-center justify-between mb-4">
            <div className="w-10"></div> {/* Spacer for centering */}
            <div className="flex items-center justify-center gap-2">
              <Activity className="w-6 h-6" />
              <h1 className="text-xl font-bold tracking-tight">Registro Diario</h1>
            </div>
            <button 
              onClick={() => setIsHistoryOpen(true)}
              className="w-10 h-10 flex items-center justify-center hover:bg-emerald-700 rounded-full transition-colors active:scale-95"
              aria-label="Ver historial"
            >
              <History className="w-5 h-5" />
            </button>
          </div>
          
          {/* Date Selector */}
          <div className="flex items-center justify-between bg-emerald-700/50 rounded-2xl p-1 backdrop-blur-sm">
            <button 
              onClick={() => changeDate(-1)}
              className="p-2 hover:bg-emerald-600 rounded-xl transition-colors active:scale-95"
              aria-label="Día anterior"
            >
              <ChevronLeft className="w-6 h-6" />
            </button>
            
            <div className="flex items-center gap-2 font-medium">
              <CalendarIcon className="w-4 h-4 opacity-80" />
              <span className="capitalize">{displayDate}</span>
              {/* Hidden native date picker for easy jumping */}
              <input 
                type="date" 
                value={selectedDate}
                onChange={(e) => setSelectedDate(e.target.value)}
                className="absolute opacity-0 w-32 h-8 cursor-pointer"
              />
            </div>

            <button 
              onClick={() => changeDate(1)}
              className="p-2 hover:bg-emerald-600 rounded-xl transition-colors active:scale-95"
              aria-label="Día siguiente"
            >
              <ChevronRight className="w-6 h-6" />
            </button>
          </div>
        </header>

        {/* Main Content */}
        <main className="flex-1 p-4 overflow-y-auto pb-32">
          {currentActivities.length === 0 ? (
            <div className="flex flex-col items-center justify-center h-full text-gray-400 gap-3 mt-12">
              <div className="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center">
                <Activity className="w-8 h-8 text-gray-300" />
              </div>
              <p className="text-center">No hay actividades para esta fecha.<br/>¡Agrega una abajo!</p>
            </div>
          ) : (
            <ul className="space-y-3">
              {currentActivities.map((activity) => (
                <li 
                  key={activity.id} 
                  className="bg-white border border-gray-100 shadow-sm rounded-2xl p-4 flex items-center justify-between gap-4 transition-all hover:shadow-md"
                >
                  <div className="flex-1 min-w-0">
                    <h3 className="font-semibold text-gray-800 truncate text-lg">
                      {activity.name}
                    </h3>
                    <input
                      type="text"
                      value={activity.note || ''}
                      onChange={(e) => updateNote(activity.id, e.target.value)}
                      placeholder="Agregar nota (opcional)..."
                      className="w-full text-sm text-gray-500 mt-0.5 bg-transparent border-none p-0 focus:ring-0 placeholder:text-gray-300 outline-none"
                    />
                  </div>
                  
                  <div className="flex items-center gap-3 bg-gray-50 p-1.5 rounded-xl border border-gray-100">
                    <button
                      onClick={() => updateCount(activity.id, -1)}
                      disabled={activity.count === 0}
                      className="w-10 h-10 flex items-center justify-center rounded-lg bg-white text-gray-600 shadow-sm hover:bg-gray-50 disabled:opacity-50 disabled:shadow-none active:scale-95 transition-all"
                      aria-label="Restar"
                    >
                      <Minus className="w-5 h-5" />
                    </button>
                    
                    <span className="w-10 text-center font-bold text-xl text-emerald-700 tabular-nums">
                      {activity.count}
                    </span>
                    
                    <button
                      onClick={() => updateCount(activity.id, 1)}
                      className="w-10 h-10 flex items-center justify-center rounded-lg bg-emerald-100 text-emerald-700 shadow-sm hover:bg-emerald-200 active:scale-95 transition-all"
                      aria-label="Sumar"
                    >
                      <Plus className="w-5 h-5" />
                    </button>
                  </div>

                  <button
                    onClick={() => deleteActivity(activity.id)}
                    className="p-2 text-gray-300 hover:text-red-500 transition-colors rounded-lg hover:bg-red-50"
                    aria-label="Eliminar actividad"
                  >
                    <Trash2 className="w-5 h-5" />
                  </button>
                </li>
              ))}
            </ul>
          )}
        </main>

        {/* Add Activity Form (Fixed Bottom) */}
        <div className="absolute bottom-0 left-0 right-0 bg-white border-t border-gray-100 p-4 shadow-[0_-10px_40px_-15px_rgba(0,0,0,0.1)] z-10">
          <form onSubmit={handleAddActivity} className="flex gap-2">
            <input
              type="text"
              value={newActivityName}
              onChange={(e) => setNewActivityName(e.target.value)}
              placeholder="Ej. Abdominales, Pesas..."
              className="flex-1 bg-gray-50 border border-gray-200 rounded-xl px-4 py-3 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all"
            />
            <button
              type="submit"
              disabled={!newActivityName.trim()}
              className="bg-emerald-600 text-white px-5 py-3 rounded-xl font-medium hover:bg-emerald-700 disabled:opacity-50 disabled:hover:bg-emerald-600 transition-all active:scale-95 flex items-center gap-2 shadow-sm"
            >
              <Plus className="w-5 h-5" />
              <span className="hidden sm:inline">Agregar</span>
            </button>
          </form>
        </div>

        {/* History Modal */}
        {isHistoryOpen && (
          <div className="absolute inset-0 bg-gray-50 z-50 flex flex-col animate-in slide-in-from-bottom-4 fade-in duration-200">
            <header className="bg-emerald-600 text-white p-4 shadow-md flex items-center justify-between sticky top-0">
              <div className="flex items-center gap-3">
                <button 
                  onClick={() => setIsHistoryOpen(false)}
                  className="p-2 hover:bg-emerald-700 rounded-full transition-colors active:scale-95 -ml-2"
                  aria-label="Cerrar historial"
                >
                  <X className="w-6 h-6" />
                </button>
                <h2 className="text-xl font-bold tracking-tight">Historial</h2>
              </div>
            </header>
            
            <main className="flex-1 overflow-y-auto p-4">
              {historyDates.length === 0 ? (
                <div className="flex flex-col items-center justify-center h-full text-gray-400 gap-3">
                  <History className="w-12 h-12 text-gray-300" />
                  <p className="text-center">Aún no hay registros en el historial.</p>
                </div>
              ) : (
                <div className="space-y-6 pb-8">
                  {historyDates.map((date) => (
                    <div key={date} className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
                      <div className="bg-emerald-50 px-4 py-3 border-b border-emerald-100 flex items-center justify-between">
                        <h3 className="font-semibold text-emerald-800 capitalize">
                          {format(parseISO(date), "EEEE, d 'de' MMMM yyyy", { locale: es })}
                        </h3>
                        <button 
                          onClick={() => {
                            setSelectedDate(date);
                            setIsHistoryOpen(false);
                          }}
                          className="text-sm text-emerald-600 hover:text-emerald-700 font-medium px-3 py-1 rounded-lg hover:bg-emerald-100 transition-colors"
                        >
                          Ver día
                        </button>
                      </div>
                      <ul className="divide-y divide-gray-50">
                        {data[date].map((activity) => (
                          <li key={activity.id} className="px-4 py-3 flex justify-between items-center gap-4">
                            <div className="flex flex-col min-w-0 flex-1">
                              <span className="text-gray-700 font-medium truncate">{activity.name}</span>
                              {activity.note && (
                                <span className="text-sm text-gray-500 truncate">{activity.note}</span>
                              )}
                            </div>
                            <span className="bg-gray-100 text-gray-700 px-3 py-1 rounded-full text-sm font-bold tabular-nums shrink-0">
                              {activity.count}
                            </span>
                          </li>
                        ))}
                      </ul>
                    </div>
                  ))}
                </div>
              )}
            </main>
          </div>
        )}

      </div>
    </div>
  );
}
