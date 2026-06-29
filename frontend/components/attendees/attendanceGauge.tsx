type AttendanceGaugeProps = {
    present: number;
    total: number;
}

const attendanceGauge: React.FC<AttendanceGaugeProps> = ({ present, total }) => {
    const percentage = total > 0 ? (present/total) * 100 : 0;

    const angle = -90 + (percentage / 100) * 180;

    const getColor = (percent: number):string => {
        if (percent >=80) return '#22c55e';
        if (percent >= 60) return '#84cc16'; 
        if (percent >= 40) return '#eab308'; 
        if (percent >= 20) return '#f97316'; 
        return '#ef4444'; 
    };

    const color = getColor(percentage);

    const createGradient = () => {
        const stops = [
        { offset: '0%', color: '#ef4444' },    
        { offset: '25%', color: '#f97316' },   
        { offset: '75%', color: '#84cc16' },   
        { offset: '100%', color: '#22c55e' }   
        ];
        return stops;
    };

    return (
        <div className="flex flex-col items-center">
            <svg width="200" height="120" viewBox="0 0 200 120">
                <defs>
                    <linearGradient id="gaugeGradient" x1="0%" y1="0%" x2="100%" y2="0%">
                        {createGradient().map((stop, index) => (
                            <stop key={index} offset={stop.offset} stopColor={stop.color} />
                        ))}
                    </linearGradient>
                </defs>

                <path
                    d="M 20 100 A 80 80 0 0 1 180 100"
                    fill="none"
                    stroke="#e5e7eb"
                    strokeWidth="20"
                    strokeLinecap="round"
                />

                <path
                    d="M 20 100 A 80 80 0 0 1 180 100"
                    fill="none"
                    stroke="url(#gaugeGradient)"
                    strokeWidth="20"
                    strokeLinecap="round"
                    strokeDasharray={`${(percentage/100) * 251.2}, 251.2`}
                />

                <circle cx="100" cy="100" r="8" fill="#4b5563" />

                <line
                    x1="100"
                    y1="100"
                    x2="100"
                    y2="30"
                    stroke="#1f2937"
                    strokeWidth="3"
                    strokeLinecap="round"
                    transform={`rotate(${angle} 100 100)`}
                />

                <text x="15" y="155" fontSize="14" fill="#6b7280" textAnchor="middle">
                    0
                </text>

                <text x="185" y="115" fontSize="14" fill="#6b7280" textAnchor="middle">
                    {total}
                </text>
            </svg>

            <div className="text-center mt-2">
                <div className="text-3xl font-bold" style={{ color }}>
                    {present}
                </div>
                <div className="text-sm text-gray-600">
                    Present out of {total}
                </div>
                <div className="text-lg font-semibold text-gray-700 mt-1">
                    {percentage.toFixed(0)}%
                </div>
            </div>
        </div>
    )
    
}

export default attendanceGauge;